module com.app.cherry {
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires annotations;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires jakarta.persistence;
    requires java.sql;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.fxmisc.flowless;
    requires atlantafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.scenicview.scenicview;
    //requires org.scenicview.scenicview;

    opens com.app.cherry.entity;
    exports com.app.cherry;
    opens com.app.cherry;
    exports com.app.cherry.controllers;
    opens com.app.cherry.controllers;
    exports com.app.cherry.util;
    opens com.app.cherry.util;
    exports com.app.cherry.controls;
    opens com.app.cherry.controls;
    exports com.app.cherry.controls.TreeViewItems;
    opens com.app.cherry.controls.TreeViewItems;
    exports com.app.cherry.controls.codearea;
    opens com.app.cherry.controls.codearea;
}