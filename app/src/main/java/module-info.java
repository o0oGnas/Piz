module app {
	requires javafx.fxml;
	requires javafx.controls;
	requires javafx.media;
	requires controlsfx;
	requires tornadofx.controls;
	requires org.slf4j;
	requires eventbus;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires org.apache.commons.io;
	requires zip4j;
	requires org.testfx;
	requires org.testfx.junit5;
	requires org.junit.jupiter.api;
	requires org.assertj.core;
	requires hamcrest.core;

	exports xyz.gnas.piz.app;
}