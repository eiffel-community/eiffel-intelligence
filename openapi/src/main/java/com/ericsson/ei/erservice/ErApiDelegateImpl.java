package com.ericsson.ei.erservice;

import com.ericsson.ei.erservice.api.EventsApiDelegate;
import com.ericsson.ei.erservice.api.SearchApiDelegate;
import com.ericsson.ei.erservice.model.EiffelEvent;
import com.ericsson.ei.erservice.model.EventsResponse;
import com.ericsson.ei.erservice.model.SearchParameters;
import com.ericsson.ei.erservice.model.SearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@ConditionalOnProperty(name = "er.security.permitAll", havingValue = "true")
public class ErApiDelegateImpl implements EventsApiDelegate, SearchApiDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErApiDelegateImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final List<String> events = new ArrayList<>();
    private static final Map<String, JsonNode> eventsById = new LinkedHashMap<>();

    static {
        loadEventsFromClasspath("eventrepository/events");
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    public static void loadEventsFromClasspath(String resourceDir) {
        events.clear();
        eventsById.clear();
        try {
            java.net.URL url = ClassLoader.getSystemResource(resourceDir);
            if (url == null) return;
            File directory = new File(url.toURI());
            File[] files = directory.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".json")) continue;
                String content = Files.readString(file.toPath());
                events.add(content);
                JsonNode event = mapper.readTree(content);
                String id = event.at("/meta/id").asText();
                if (!id.isEmpty()) {
                    eventsById.put(id, event);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load events from classpath", e);
        }
    }

    @Override
    public ResponseEntity<EiffelEvent> getEventUsingGET(String id) {
        for (String event : events) {
            try {
                Object document = Configuration.defaultConfiguration().jsonProvider().parse(event);
                Object value = JsonPath.read(document, "$.meta.id");
                if (id.equals(value)) {
                    return new ResponseEntity<>(mapper.readValue(event, EiffelEvent.class), HttpStatus.OK);
                }
            } catch (PathNotFoundException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<EventsResponse> getEventsUsingGET(Integer pageSize, Map<String, String> params) {
        String[] keysToIgnore = {"pageSize", "shallow"};
        List<EiffelEvent> matchedEvents = new ArrayList<>();

        for (String event : events) {
            boolean matches = true;
            try {
                Object document = Configuration.defaultConfiguration().jsonProvider().parse(event);
                for (String key : params.keySet()) {
                    boolean skip = false;
                    for (String ignore : keysToIgnore) {
                        if (key.equals(ignore)) { skip = true; break; }
                    }
                    if (skip) continue;

                    String expected = params.get(key);
                    Object value = JsonPath.read(document, "$." + key);
                    if (expected != null && !expected.isEmpty() && !value.toString().equals(expected)) {
                        matches = false;
                        break;
                    }
                }
            } catch (PathNotFoundException e) {
                matches = false;
            } catch (Exception e) {
                matches = false;
            }

            if (matches) {
                try {
                    matchedEvents.add(mapper.readValue(event, EiffelEvent.class));
                } catch (Exception e) {
                    // skip
                }
            }
        }

        EventsResponse response = new EventsResponse();
        response.pageNo(1).pageSize(pageSize).totalNumberItems(matchedEvents.size()).items(matchedEvents);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchResponse> searchUsingPOST(String id, Integer limit, Integer levels,
            Boolean tree, Boolean shallow, SearchParameters searchParameters) {

        JsonNode startEvent = eventsById.get(id);
        if (startEvent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<String> ult = extractLinkTypes(searchParameters, true);
        List<String> dlt = extractLinkTypes(searchParameters, false);
        int maxLevels = normalizeParam(levels);
        int maxEvents = normalizeParam(limit);
        boolean useTree = Boolean.TRUE.equals(tree);

        List<Object> upstream = traverseLinks(id, startEvent, ult, maxLevels, maxEvents, useTree, true);
        List<Object> downstream = traverseLinks(id, startEvent, dlt, maxLevels, maxEvents, useTree, false);

        SearchResponse response = new SearchResponse();
        response.setUpstreamLinkObjects(upstream);
        response.setDownstreamLinkObjects(downstream);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<String> extractLinkTypes(SearchParameters params, boolean upstream) {
        if (params == null) return Collections.emptyList();
        List<String> types = upstream ? params.getUlt() : params.getDlt();
        return types != null ? types : Collections.emptyList();
    }

    private int normalizeParam(Integer value) {
        return (value == null || value <= 0) ? Integer.MAX_VALUE : value;
    }

    private List<Object> traverseLinks(String id, JsonNode startEvent, List<String> linkTypes,
            int maxLevels, int maxEvents, boolean useTree, boolean upstream) {
        if (linkTypes.isEmpty()) return new ArrayList<>();
        Set<String> visited = new HashSet<>();
        visited.add(id);
        if (useTree) {
            List<Object> tree = upstream
                    ? buildUpstreamTree(startEvent, linkTypes, maxLevels, maxEvents, visited)
                    : buildDownstreamTree(startEvent, linkTypes, maxLevels, maxEvents, visited);
            return tree != null ? tree : new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        if (upstream) {
            collectUpstreamFlat(startEvent, linkTypes, maxLevels, maxEvents, visited, result);
        } else {
            collectDownstreamFlat(startEvent, linkTypes, maxLevels, maxEvents, visited, result);
        }
        return result;
    }

    private List<Object> buildUpstreamTree(JsonNode event, List<String> linkTypes, int maxLevels, int maxEvents, Set<String> visited) {
        if (maxLevels <= 0) return null;

        List<Object> tree = new ArrayList<>();
        tree.add(mapper.convertValue(event, Map.class));

        for (String target : getMatchingLinkTargets(event, linkTypes)) {
            if (visited.contains(target)) continue;
            JsonNode targetEvent = eventsById.get(target);
            if (targetEvent == null) continue;

            visited.add(target);
            List<Object> subtree = buildUpstreamTree(targetEvent, linkTypes, maxLevels - 1, maxEvents, visited);
            if (subtree != null) tree.add(subtree);
        }
        return tree;
    }

    private List<Object> buildDownstreamTree(JsonNode event, List<String> linkTypes, int maxLevels, int maxEvents, Set<String> visited) {
        if (maxLevels <= 0) return null;

        String eventId = event.at("/meta/id").asText();
        List<Object> tree = new ArrayList<>();
        tree.add(mapper.convertValue(event, Map.class));

        for (Map.Entry<String, JsonNode> entry : eventsById.entrySet()) {
            String candidateId = entry.getKey();
            if (visited.contains(candidateId)) continue;

            if (hasLinkTo(entry.getValue(), eventId, linkTypes)) {
                visited.add(candidateId);
                List<Object> subtree = buildDownstreamTree(entry.getValue(), linkTypes, maxLevels - 1, maxEvents, visited);
                if (subtree != null) tree.add(subtree);
            }
        }
        return tree;
    }

    private boolean hasLinkTo(JsonNode candidate, String targetId, List<String> linkTypes) {
        JsonNode links = candidate.get("links");
        if (links == null || !links.isArray()) return false;
        for (JsonNode link : links) {
            String type = link.has("type") ? link.get("type").asText() : "";
            String target = link.has("target") ? link.get("target").asText() : "";
            if (targetId.equals(target) && matchesLinkType(type, linkTypes)) return true;
        }
        return false;
    }

    private void collectUpstreamFlat(JsonNode event, List<String> linkTypes, int maxLevels, int maxEvents, Set<String> visited, List<Object> result) {
        if (maxLevels <= 0 || result.size() >= maxEvents) return;

        for (String targetId : getMatchingLinkTargets(event, linkTypes)) {
            if (visited.contains(targetId)) continue;
            JsonNode targetEvent = eventsById.get(targetId);
            if (targetEvent == null) continue;

            visited.add(targetId);
            result.add(mapper.convertValue(targetEvent, Map.class));
            if (result.size() < maxEvents) {
                collectUpstreamFlat(targetEvent, linkTypes, maxLevels - 1, maxEvents, visited, result);
            }
        }
    }

    private void collectDownstreamFlat(JsonNode event, List<String> linkTypes, int maxLevels, int maxEvents, Set<String> visited, List<Object> result) {
        if (maxLevels <= 0 || result.size() >= maxEvents) return;
        String eventId = event.at("/meta/id").asText();

        for (Map.Entry<String, JsonNode> entry : eventsById.entrySet()) {
            if (result.size() >= maxEvents) break;
            if (visited.contains(entry.getKey())) continue;
            if (!hasLinkTo(entry.getValue(), eventId, linkTypes)) continue;

            visited.add(entry.getKey());
            result.add(mapper.convertValue(entry.getValue(), Map.class));
            collectDownstreamFlat(entry.getValue(), linkTypes, maxLevels - 1, maxEvents, visited, result);
        }
    }

    private List<String> getMatchingLinkTargets(JsonNode event, List<String> linkTypes) {
        List<String> targets = new ArrayList<>();
        JsonNode links = event.get("links");
        if (links == null || !links.isArray()) return targets;
        for (JsonNode link : links) {
            String type = link.has("type") ? link.get("type").asText() : "";
            String target = link.has("target") ? link.get("target").asText() : "";
            if (!target.isEmpty() && matchesLinkType(type, linkTypes)) {
                targets.add(target);
            }
        }
        return targets;
    }

    private boolean matchesLinkType(String type, List<String> allowedTypes) {
        if (allowedTypes.contains("ALL")) return true;
        return allowedTypes.contains(type);
    }
}
