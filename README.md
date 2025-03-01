# hiking-playground
Preparation for the EDD workshop


- `mvn -DapiSpec=api1.yml clean install`

- `mvn -DapiSpec=api2.yml clean install`

- `mvn -DapiSpec=api3.yml clean install`








```
pandoc -s --pdf-engine=xelatex -f markdown -t beamer --slide-level=2 -o pbat-slides.pdf metadata-beamer.yml pbat-slides.md --filter=pandoc-crossref --highlight-style=haddock --citeproc
```
