package br.com.equilibra.attachment.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.file.Path;
import java.util.List;

@ConfigurationProperties(prefix="equilibra.attachments")
public class AttachmentProperties {
    private String storageType="local"; private String localRoot="var/attachments"; private long maxFileSize=10*1024*1024; private int maxAttachmentsPerTransaction=10; private List<String> allowedMediaTypes=List.of("application/pdf","image/jpeg","image/png","image/webp");
    public String getStorageType(){return storageType;} public void setStorageType(String v){storageType=v;}
    public String getLocalRoot(){return localRoot;} public void setLocalRoot(String v){localRoot=v;}
    public Path localRootPath(){return Path.of(localRoot).toAbsolutePath().normalize();}
    public long getMaxFileSize(){return maxFileSize;} public void setMaxFileSize(long v){maxFileSize=v;}
    public int getMaxAttachmentsPerTransaction(){return maxAttachmentsPerTransaction;} public void setMaxAttachmentsPerTransaction(int v){maxAttachmentsPerTransaction=v;}
    public List<String> getAllowedMediaTypes(){return allowedMediaTypes;} public void setAllowedMediaTypes(List<String> v){allowedMediaTypes=v;}
}
