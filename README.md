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

Der Runner kann mit folgenden Befehlen ausgeführt werden:
```bash
java -jar interlis-testbed-runner.jar --validator <Pfad zu ilivalidator.jar> <Pfad zum Testbed-Ordner (Standard: aktueller Ordner)>
java -jar interlis-testbed-runner.jar -v <Pfad zu ilivalidator.jar> --config <Pfad zu ilivalidator config> <Pfad zum Testbed-Ordner (Standard: aktueller Ordner)>
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

Zusätzlich zu einfachen XTF Dateien können auch INTERLIS 2.4 XTF Dateien mit inkrementeller Syntax verwendet werden. Es gelten im gegensatz zur Spezifikation die angenommenen Standardwerte `ili:kind="UPDATE"` und `ili:operation="UPDATE"` um mit der vereinfachten Syntax kompatibel zu sein. Für den Update eines Baskets wird lediglich `ili:kind="UPDATE"` unterstützt.

Beispiel `FailCase.xtf` (INTERLIS 2.4):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ili:transfer xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS" xmlns="http://www.interlis.ch/xtf/2.4/Model">
    <ili:datasection>
        <Topic ili:bid="<basket id>">
            <!-- Das Objekt mit dieser TID wird hinzugefügt oder überschrieben -->
            <Class ili:tid="<object id>">
                <!-- Attributes -->
            </Class>
        </Topic>
        <Topic ili:bid="<basket id>" ili:kind="UPDATE">
            <!-- Ein neues Objekt wird hinzugefügt -->
            <Class ili:tid="<new object id>" ili:operation="INSERT">
                <!-- Attributes -->
            </Class>
            <!-- Ein bestehendes Objekt wird überschrieben -->
            <Class ili:tid="<existing object id>" ili:operation="UPDATE">
                <!-- Attributes -->
            </Class>
    </ili:datasection>
</ili:transfer>
```

### Entfernen von Elementen
Um für einen Fail-Case ein Objekt aus einem Basket oder einen kompletten Basket zu entfernen, kann dem Element ein `delete`-Attribut hinzugefügt werden. Für das Entfernen von Objekten kann ebenfalls die inkrementelle Syntax von INTERLIS 2.4 verwendet werden.

Beispiel `FailCase.xtf` (INTERLIS 2.4):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ili:transfer xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS" xmlns="http://www.interlis.ch/xtf/2.4/Model">
    <ili:datasection>
        <Topic ili:bid="<basket id>">
            <!-- Das Objekt mit dieser TID wird aus dem Basket entfernt -->
            <Class ili:tid="<object id>" delete="" />
            <Class ili:tid="<object id>" ili:operation="DELETE" />
            <ili:delete ili:tid="<object id>" />
        </Topic>
        <!-- Der gesamte Basket wird entfernt -->
        <Topic ili:bid="<basket id>" delete="">
    </ili:datasection>
</ili:transfer>
```

## API
Einige Funktionalitäten des Runners können auch über das Java API verwendet werden, beispielsweise zur direkten Integration in einem Unit-Test.

### Zusammenfügen von XTF-Dateien
Das Zusammenfügen der Fail-Cases mit den Basisdaten kann mit der Klasse `XtfFileMerger` durchgeführt werden.
Die Methode `merge` erwartet die Pfade zu den Basisdaten, den Anpassungen des Fail-Cases und der Ausgabedatei.
Die XTF-Datei des Fail-Cases ist dabei gleich aufgebaut wie bei der automatischen Ordner-basierten Ausführung des Runners.

Beispiel:
```java
import ch.geowerkstatt.interlis.testbed.runner.xtf.XtfFileMerger;

//...

void example() {
    XtfFileMerger xtfMerger = new XtfFileMerger();
    xtfMerger.merge(Path.of("path", "to", "base.xtf"), Path.of("path", "to", "failcase.xtf"), Path.of("path", "to", "output.xtf"));
}
```

### Prüfen von Constraint-Fehlern
Um zu prüfen, ob mindestens ein Fehler zu einem spezifischen Constraint im Log vorkommt, kann die Klasse `IliValidatorLogParser` verwendet werden.
Die statische Methode `containsConstraintError` erwartet dazu den Pfad zur Log-Datei sowie den voll-qualifizierten Namen des Constraints.

Beispiel:
```java
import ch.geowerkstatt.interlis.testbed.runner.validation.IliValidatorLogParser;

//...

void example() {
    boolean hasError = IliValidatorLogParser.containsConstraintError(Path.of("path", "to", "validator-log.log"), "Model.Topic.Class.Constraint");
}
```
