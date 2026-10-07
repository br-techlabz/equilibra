package br.com.equilibra.attachment.infrastructure;

import br.com.equilibra.attachment.application.*;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Component
public class LocalFileSystemAttachmentStorage implements AttachmentStorage {
    private final Path root;
    public LocalFileSystemAttachmentStorage(AttachmentProperties properties){root=properties.localRootPath();}
    private Path resolve(String key){
        if(key==null||key.isBlank()||key.contains("..")||key.startsWith("/")||key.contains("\\")) throw new IllegalArgumentException("Invalid storage key");
        Path resolved=root.resolve(key).normalize(); if(!resolved.startsWith(root))throw new IllegalArgumentException("Storage key escapes root"); return resolved;
    }
    public void store(String key,InputStream content,long size)throws IOException{Path target=resolve(key);if(Files.exists(target))throw new FileAlreadyExistsException(key);Files.createDirectories(target.getParent());Path temp=target.resolveSibling("."+UUID.randomUUID()+".tmp");try(InputStream in=content;OutputStream out=Files.newOutputStream(temp,StandardOpenOption.CREATE_NEW)){in.transferTo(out);}try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,target);}catch(Exception e){Files.deleteIfExists(temp);throw e;}}
    public InputStream open(String key)throws IOException{return Files.newInputStream(resolve(key),StandardOpenOption.READ);}
    public boolean exists(String key)throws IOException{return Files.exists(resolve(key));}
    public void delete(String key)throws IOException{Files.deleteIfExists(resolve(key));}
}
