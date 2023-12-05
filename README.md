# interlis-testbed-runner
Der Testbed-Runner ermöglicht das Testen von Constraints bzw. der dazugehörigen Methoden basierend auf Testdaten in einer definierten Ordnerstruktur.

## Anwendung
Der Runner funktioniert generisch auf einer entsprechenden Verzeichnisstruktur und globalen ilivalidator-Installation.

Diese Struktur ist folgendermassen aufgebaut:

```
TestSuiteA
    ModelToTest.ili
    Successful_Data.xtf
    (ilivalidator-config.toml)
    ModelA.TopicA.ClassA.Constraint1
        FailCase-1.xtf
        FailCase-2.xtf
        …
    ModelA.TopicA.ClassB.Constraint2
        FailCase-1.xtf
        …
    Output
        ModelA.TopicA.ClassA.Constraint1
            FailCase-1_Merged.xtf
            FailCase-1.log
```
