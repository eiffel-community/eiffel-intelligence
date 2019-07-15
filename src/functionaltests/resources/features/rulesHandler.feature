#Author: christoffer.cortes.sjowall@ericsson.com

@RulesHandler
Feature: Test Rules Handler

  @RulesHandlerRelativePath
  Scenario: Test relative path
  Given a file with path "/rules/ArtifactRules-Eiffel-Agen-Version.json"
  Then rules are loaded

  @RulesHandlerFullPath
  Scenario: Test full path
  Given a file with path "src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json"
  And path is made absolute
  Then rules are loaded

  @RulesHandlerFileURI
  Scenario: Test URI with file scheme
  Given a file with path "src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json"
  And path is made absolute
  And path is URI with "file:///" scheme
  Then rules are loaded

  @RulesHandlerHttpURI
  Scenario: Test URI with http scheme
  Given a file with path "localhost:{port}/some/route/MyRules.json"
  And path is URI with "http://" scheme
  Then rules are loaded

  @RulesHandlerIncorrectPath
  Scenario: Test incorrect rules path
  Given a file with path "localhost:{port}/wrong/route/MyRules.json"
  And path is URI with "http://" scheme
  Then rules are loaded with expected exception

  @RulesHandlerEmptyRules
  Scenario: Test empty rules file
  Given a file with path "localhost:{port}/wrong/route/EmptyRules.json"
  And path is URI with "http://" scheme
  Then rules are loaded with expected exception