module xyz.gnas.piz.core {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;

    requires transitive zip4j;

    exports xyz.gnas.piz.core.logic;
    exports xyz.gnas.piz.core.models;
    exports xyz.gnas.piz.core.models.zip;
}