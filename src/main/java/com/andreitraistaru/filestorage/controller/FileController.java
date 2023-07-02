package com.andreitraistaru.filestorage.controller;

import com.andreitraistaru.filestorage.exceptions.AlreadyExistingStorageItemException;
import com.andreitraistaru.filestorage.exceptions.InvalidStorageItemNameException;
import com.andreitraistaru.filestorage.exceptions.MissingStorageItemException;
import com.andreitraistaru.filestorage.exceptions.StorageServiceException;
import com.andreitraistaru.filestorage.service.StorageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Log4j2
public class FileController {
    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile newFile) {
        try {
            storageService.createStorageItem(filename, newFile);
        } catch(AlreadyExistingStorageItemException ignored) {
            return new ResponseEntity<>("File " + filename + " already existing. Storage system " +
                    "has not been modified. Try again using /update endpoint.", HttpStatus.CONFLICT);
        } catch(InvalidStorageItemNameException ignored) {
            return new ResponseEntity<>(filename + " is an invalid file name. File name should contain" +
                    " 1-64 characters from [a-z][A-Z][0-9]_-", HttpStatus.BAD_REQUEST);
        } catch (StorageServiceException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("File " + filename + " created successfully.", HttpStatus.CREATED);
    }

    @GetMapping("/read")
    public ResponseEntity<?> readFile(@RequestParam("filename") String filename) {
        Resource storageItemResource;

        try {
            storageItemResource = storageService.readStorageItem(filename);
        } catch(MissingStorageItemException ignored) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(InvalidStorageItemNameException ignored) {
            return new ResponseEntity<>(filename + " is an invalid file name. File name should contain" +
                    " 1-64 characters from [a-z][A-Z][0-9]_-", HttpStatus.BAD_REQUEST);
        } catch (StorageServiceException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(storageItemResource);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile updatedFile) {
        try {
            storageService.updateStorageItem(filename, updatedFile);
        } catch(MissingStorageItemException ignored) {
            return new ResponseEntity<>("File " + filename + " not existing. Storage system " +
                    "has not been modified. Try again using /create endpoint.", HttpStatus.NOT_FOUND);
        }  catch(InvalidStorageItemNameException ignored) {
            return new ResponseEntity<>(filename + " is an invalid file name. File name should contain" +
                    " 1-64 characters from [a-z][A-Z][0-9]_-", HttpStatus.BAD_REQUEST);
        } catch (StorageServiceException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("File " + filename + " updated successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename) {
        try {
            storageService.deleteStorageItem(filename);
        } catch(MissingStorageItemException ignored) {
            return new ResponseEntity<>("File " + filename + " not existing.", HttpStatus.NOT_FOUND);
        } catch(InvalidStorageItemNameException ignored) {
            return new ResponseEntity<>(filename + " is an invalid file name. File name should contain" +
                    " 1-64 characters from [a-z][A-Z][0-9]_-", HttpStatus.BAD_REQUEST);
        } catch (StorageServiceException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("File " + filename + " deleted successfully.", HttpStatus.OK);
    }
}
