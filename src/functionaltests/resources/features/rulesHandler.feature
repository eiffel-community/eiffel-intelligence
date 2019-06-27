#Author: christoffer.cortes.sjowall@ericsson.com

@RulesHandler
Feature: Test Rules Handler

  @RulesHandlerRelativePath
  Scenario: Test relative path
  Given a file with path "src/main/resources/ArtifactRules.json"
  Then rules are loaded

  @RulesHandlerFullPath
  Scenario: Test full path
  Given a file with path "src/main/resources/ArtifactRules.json"
  And path should be absolute
  Then rules are loaded

  @RulesHandlerFileURI
  Scenario: Test URI with file scheme
  Given a file with path "src/main/resources/ArtifactRules.json"
  And path should be absolute
  And path is URI with "file" scheme
  Then rules are loaded

  @RulesHandlerHttpURI
  Scenario: Test URI with file scheme
  Given a file with path "localhost:{port}/some/route/MyRules.json"
  And path is URI with "http" scheme
  Then rules are loaded

  @RulesHandlerHttpURI
  Scenario: Test URI with file scheme
  Given a file with path "localhost:{port}/wrong/route/MyRules.json"
  And path is URI with "http" scheme
  Then rules are loaded with expected exception

    @RulesHandlerHttpURI
  Scenario: Test URI with file scheme
  Given a file with path "localhost:{port}/wrong/route/EmptyRules.json"
  And path is URI with "http" scheme
  Then rules are loaded with expected exception