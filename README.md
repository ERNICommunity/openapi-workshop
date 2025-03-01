# hiking-playground
Preparation for the EDD workshop


- `mvn -DapiSpec=api1.yml clean install`

- `mvn -DapiSpec=api2.yml clean install`

- `mvn -DapiSpec=api3.yml clean install`








```
pandoc -s --pdf-engine=xelatex -f markdown -t beamer --slide-level=2 -o slides.pdf metadata-beamer.yml $< ${PANDOC_FILTER_PARMS} --highlight-style=haddock --citeproc
```