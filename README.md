# interlis-testbed-runner
Der Testbed-Runner ermöglicht das Testen von Constraints bzw. der dazugehörigen Methoden basierend auf Testdaten in einer definierten Ordnerstruktur.

## Anwendung
Der Runner funktioniert generisch auf einer entsprechenden Verzeichnisstruktur mit diesem Aufbau:

```
TestSuiteA
    ModelToTest.ili
    Successful_Data.xtf
    ModelA.TopicA.ClassA.Constraint1
        FailCase-1.xtf
        FailCase-2.xtf
        …
    ModelA.TopicA.ClassB.Constraint2
        FailCase-1.xtf
        …
    Output
        Successful_Data.log
        ModelA.TopicA.ClassA.Constraint1
            FailCase-1_Merged.xtf
            FailCase-1.log
            …
```

Der Runner kann mit folgendem Befehl ausgeführt werden:
```bash
java -jar interlis-testbed-runner.jar --validator <Pfad zu ilivalidator.jar> <Pfad zum Testbed-Ordner (Standard: aktueller Ordner)>
```

Der Runner führt dabei folgende Schritte aus:
- Die XTF-Datei der Basisdaten wird geprüft und muss gemäss Modell gültig sein
- Jede XTF-Datei für einen Fail-Case wird mit den Basisdaten zusammengefügt und im Output-Ordner abgelegt
- Es wird geprüft, dass die zusammengefügten XTF-Dateien gemäss Modell ungültig sind und jeweils mindestens ein Fehler für den Constraint in der Log-Datei vorhanden ist (der Constraint-Name entspricht dabei dem Ordner-Namen vom Fail-Case)

## Aufbau der XTF-Dateien für Failcases
Der Runner kann INTERLIS XTF-Dateien ohne XML-Namespaces oder mit INTERLIS 2.4 Namespace verarbeiten.
Die Header-Section kann bei den Fail-Case Dateien weggelassen werden, da sie von den Basisdaten übernommen wird.

### Hinzufügen oder ersetzen von Elementen
Um für einen Fail-Case ein Objekt zu einem Basket hinzuzufügen oder zu überschreiben, kann es wie in einer XTF-Datei üblich beschrieben werden.
Falls das Objekt mit der angegebenen `TID` im Basket vorhanden ist, wird es mit den hier definierten Attributen überschrieben, ansonsten zum Basket hinzugefügt.

Beispiel `FailCase.xtf` (INTERLIS 2.4):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ili:transfer xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS" xmlns="http://www.interlis.ch/xtf/2.4/Model">
    <ili:datasection>
        <Topic ili:bid="<basket id>">
            <!-- Das Objekt mit dieser TID wird hinzugefügt oder überschrieben -->
            <Class ili:tid="<object id>">
                <!-- Attribute -->
            </Class>
        </Topic>
    </ili:datasection>
</ili:transfer>
```

### Entfernen von Elementen
Um für einen Fail-Case ein Objekt aus einem Basket oder einen kompletten Basket zu entfernen, kann dem Element ein `delete`-Attribut hinzugefügt werden.

Beispiel `FailCase.xtf` (INTERLIS 2.4):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ili:transfer xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS" xmlns="http://www.interlis.ch/xtf/2.4/Model">
    <ili:datasection>
        <Topic ili:bid="<basket id>">
            <!-- Das Objekt mit dieser TID wird aus dem Basket entfernt -->
            <Class ili:tid="<object id>" delete="" />
        </Topic>
    </ili:datasection>
</ili:transfer>
```
