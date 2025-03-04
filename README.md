# hiking-playground

[![Build Badge]](https://github.com/johannes-bruegmann/hiking-playground/actions/workflows/maven.yml)

[Build Badge]: https://github.com/johannes-bruegmann/hiking-playground/actions/workflows/maven.yml/badge.svg


## EDD Workshop Aufgaben

Aufgabe 1:

Ganz einfache OpenAPI Erweiterung

- `mvn -DapiSpec=aufgaben/a1-api.yml clean install`
- `mvn -DapiSpec=aufgaben/a1-api-loesung.yml clean install`

Aufgabe 2:

Hard-Codierter Test mit Openapi Validator (isValid Test) und Jackson Serialisierung (Serialization Identity Test)

- `mvn -DapiSpec=aufgaben/a2-api.yml clean install`
- `mvn -DapiSpec=aufgaben/a2-api-loesung.yml clean install`

-----

Einführung Moustache Template

    <generateModelTests>true</generateModelTests>

    [model_test.mustache](../hiking-client-example/src/main/resources/templates/openapi/model_test.mustache)

-----

Aufgabe 3:

Komplexe Erweiterung der Api SPEC mit dem Sicherheitsnetz durch die Moustache generierten PBT Tests

- `mvn -DapiSpec=aufgaben/a3-api.yml clean install`
- `mvn -DapiSpec=aufgaben/a3-api-loesung.yml clean install`
