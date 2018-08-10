# dutchhelper

## Abandoned. https://github.com/Tbsc/Wikdget for replacement

Shows Wiktionary information about Dutch words.  
Uses [DKPro JWKTL](https://dkpro.github.io/dkpro-jwktl) to parse information from Wiktionary.

## Installation

Either use a precompiled version (if exists) or compile it yourself using Gradle (`gradle build`).  
It requires creating a database with [my fork of JWKTL](https://github.com/Tbsc/dkpro-jwktl) (how to do that is explained there).
Run dutchhelper, and as an argument give it the path to the database:  
```
java -jar dutchhelper.jar -p <path>
```
