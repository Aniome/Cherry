module com.app.cherry {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires annotations;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires jakarta.persistence;
    requires java.sql;
    requires org.jdom2;

    opens com.app.cherry.entity;
    exports com.app.cherry;
    opens com.app.cherry;
}