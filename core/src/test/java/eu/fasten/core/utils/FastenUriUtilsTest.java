package eu.fasten.core.utils;

import eu.fasten.core.data.FastenURI;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FastenUriUtilsTest {

    private String partialUriFormatException = "Invalid partial FASTEN URI. The format is corrupted.\nMust be: `/{namespace}/{class}.{method}({signature.args})/{signature.returnType}`";

    @Test
    void testGenerateFullFastenUriSuccess() {

        var forge = "forge";
        var pkg = "name";
        var version = "1.0";
        var partial = "/partial";

        var expectedFullUri = "fasten://forge!name$1.0/partial";

        var actual = FastenUriUtils.generateFullFastenUri(forge, pkg, version, partial);

        assertEquals(expectedFullUri, actual);
    }

    @Test
    void testGenerateFullFastenUriObjectSuccess() {

        var forge = "forge";
        var pkg = "name";
        var version = "1.0";
        var namespace = "namespace";
        var entity = "class.method()/void";

        var partial = "/" + namespace + "/" + entity;

        var uri = FastenUriUtils.generateFullFastenUri(forge, pkg, version, partial);
        var fastenUri = FastenURI.create(uri);

        assertEquals(forge, fastenUri.getForge());
        assertEquals(pkg, fastenUri.getProduct());
        assertEquals(version, fastenUri.getVersion());
        assertEquals(namespace, fastenUri.getNamespace());
        assertEquals(entity, fastenUri.getEntity());

    }

    @Test
    void testParseFullFastenUriSuccess() {

        var fullUri = "fasten://forge!name$1.0/partial";

        var expectedForge = "forge";
        var expectedPackage = "name";
        var expectedVersion = "1.0";
        var expectedPartial = "/partial";

        var actual = FastenUriUtils.parseFullFastenUri(fullUri);

        assertEquals(expectedForge, actual.get(0));
        assertEquals(expectedPackage, actual.get(1));
        assertEquals(expectedVersion, actual.get(2));
        assertEquals(expectedPartial, actual.get(3));
    }

    @Test
    void parseFullFastenUriTest() {
        var fullUri = "fasten://mvn!com.google.guava:guava$24.1-jre/com.google.common.collect/Synchronized$SynchronizedList.set(%2Fjava.lang%2FIntegerType,%2Fjava.lang%2FObject)%2Fjava.lang%2FObject";
        var forge = "mvn";
        var packageName = "com.google.guava:guava";
        var version = "24.1-jre";
        var uri = "/com.google.common.collect/Synchronized$SynchronizedList.set(%2Fjava.lang%2FIntegerType,%2Fjava.lang%2FObject)%2Fjava.lang%2FObject";
        var result = FastenUriUtils.parseFullFastenUri(fullUri);
        assertEquals(forge, result.get(0));
        assertEquals(packageName, result.get(1));
        assertEquals(version, result.get(2));
        assertEquals(uri, result.get(3));
    }

    @Test
    void testParsePartialFastenUriSuccess() {

        var partialUri = "/junit.awtui/AboutDialog.<init>(/java.awt/Frame)/java.lang/VoidType";

        var expectedNamespace = "junit.awtui";
        var expectedClass = "AboutDialog";
        var expectedMethod = "<init>";
        var expectedArgs = "/java.awt/Frame";
        var expectedReturnType = "/java.lang/VoidType";

        var actual = FastenUriUtils.parsePartialFastenUri(partialUri);

        assertEquals(expectedNamespace, actual.get(0));
        assertEquals(expectedClass, actual.get(1));
        assertEquals(expectedMethod, actual.get(2));
        assertEquals(expectedArgs, actual.get(3));
        assertEquals(expectedReturnType, actual.get(4));
    }

    @Test
    void testParsePartialFastenEncodedUriSuccess() {

        var partialUri = "/com.sun.istack.localization/Localizer.%3Cinit%3E(%2Fjava.util%2FLocale)%2Fjava.lang%2FVoidType";

        var expectedNamespace = "com.sun.istack.localization";
        var expectedClass = "Localizer";
        var expectedMethod = "<init>";
        var expectedArgs = "/java.util/Locale";
        var expectedReturnType = "/java.lang/VoidType";

        var actual = FastenUriUtils.parsePartialFastenUri(partialUri);

        assertEquals(expectedNamespace, actual.get(0));
        assertEquals(expectedClass, actual.get(1));
        assertEquals(expectedMethod, actual.get(2));
        assertEquals(expectedArgs, actual.get(3));
        assertEquals(expectedReturnType, actual.get(4));
    }

    @Test
    void testParsePartialFastenUriSuccessWithEmptyArgs() {
        var partialUri = "/junit.awtui/AboutDialog.<init>()/java.lang/VoidType";
        var expectedArgs = "";
        var actual = FastenUriUtils.parsePartialFastenUri(partialUri);
        assertEquals(expectedArgs, actual.get(3));
    }

    @Test
    void testParsePartialFastenUriFailFullUri() {
        var fullUriException = "Invalid partial FASTEN URI. You may want to use parser for full FASTEN URI instead.";
        var partialUri = "fasten://forge!name$1.0/partial";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FastenUriUtils.parsePartialFastenUri(partialUri);
        });

        String actualMessage = exception.getMessage();
        assertEquals(fullUriException, actualMessage);

    }

    @Test
    void testParsePartialFastenUriFailedModule() {
        var partialUri = "/junit.awtui/AboutDialog<init>(/java.awt/Frame)/java.lang/VoidType";  // missing trailing `.` after class name.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FastenUriUtils.parsePartialFastenUri(partialUri);
        });

        String actualMessage = exception.getMessage();
        assertEquals(partialUriFormatException, actualMessage);
    }

    @Test
    void testParsePartialFastenUriFailedMethodArgs() {
        var partialUri = "/junit.awtui/AboutDialog.<init>/java.awt/Frame)/java.lang/VoidType"; // missing leading `(`.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FastenUriUtils.parsePartialFastenUri(partialUri);
        });

        String actualMessage = exception.getMessage();
        assertEquals(partialUriFormatException, actualMessage);
    }

    @Test
    void testParsePartialFastenUriFailedMethodReturnT() {
        var partialUri = "/junit.awtui/AboutDialog.<init>(/java.awt/Frame)"; // missing return type.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FastenUriUtils.parsePartialFastenUri(partialUri);
        });

        String actualMessage = exception.getMessage();
        assertEquals(partialUriFormatException, actualMessage);
    }

    @Test
    void testParsePartialFastenUriFailedMissingNamespace() {
        var partialUri = "/AboutDialog.<init>(/java.awt/Frame)"; // missing namespace.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FastenUriUtils.parsePartialFastenUri(partialUri);
        });

        String actualMessage = exception.getMessage();
        assertEquals(partialUriFormatException, actualMessage);
    }

}
