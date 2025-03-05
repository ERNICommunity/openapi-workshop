# hiking-playground

[![Build Badge]](https://github.com/ERNICommunity/openapi-workshop/actions/workflows/maven.yml)

[Build Badge]: https://github.com/ERNICommunity/openapi-workshop/actions/workflows/maven.yml/badge.svg


## EDD Workshop Aufgaben

Aufgabe 1:

Ganz einfache OpenAPI Erweiterung

- `mvn -DapiSpec=aufgaben/a1-openapi.yml clean install`
- `mvn -DapiSpec=loesungen/a1-openapi.yml clean install`

Problem, das auftauchen könnte: Vertauschen der Minimal- und Maximalwerte bei Einschränkungen.  

Aufgabe 2:

Massnahme, um das Problem zu erkennen.

Hard-Codierter Test mit Openapi Validator (isValid Test) und Jackson Serialisierung (Serialization Identity Test)

- `mvn -DapiSpec=aufgaben/a2-openapi.yml clean install`
- `mvn -DapiSpec=loesungen/a2-openapi.yml clean install`

-----

Einführung openapi-generator mit eigenem Moustache Template

- Konfiguration openapi-generator-maven-plugin, mit `<generateModelTests>true</generateModelTests>`

- [model_test.mustache](../hiking-client-example/src/main/resources/templates/openapi/model_test.mustache) hinzufügen

-----

Aufgabe 3:

Komplexe Erweiterung der Api SPEC mit dem Sicherheitsnetz durch die Moustache generierten PBT Tests

- `mvn -DapiSpec=aufgaben/a3-openapi.yml clean install`
- `mvn -DapiSpec=loesungen/a3-openapi.yml clean install`

Mit dem Template werden nun Tests generiert, die auch ganz andere Probleme,
als bloss die Minimal-/Maximalwert Vertauschung erkennen.
