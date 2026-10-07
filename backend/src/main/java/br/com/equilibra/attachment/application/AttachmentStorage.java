package br.com.equilibra.attachment.application;

import java.io.InputStream;
import java.io.IOException;

public interface AttachmentStorage { void store(String key, InputStream content, long size) throws IOException; InputStream open(String key) throws IOException; boolean exists(String key) throws IOException; void delete(String key) throws IOException; }
