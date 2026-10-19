package com.studysync.model;

import com.studysync.exception.FileStorageException;
import java.io.OutputStream;

/**
 * Interface representing resources that can be streamed to a client as an attachment.
 * 
 * CONCEPT: Abstraction & Interface-Based Design
 * WHY: Provides a uniform abstraction for downloadable assets (notes, exported reports),
 * separating file streaming logic from servlet transport protocols.
 */
public interface Downloadable {

    /**
     * Streams the resource's binary content to the target output stream.
     * 
     * @param out target stream (e.g., HttpServletResponse OutputStream)
     * @throws FileStorageException if the file cannot be accessed or streamed
     */
    void download(OutputStream out) throws FileStorageException;

    /**
     * Returns the safe, human-readable filename for Content-Disposition attachment headers.
     * 
     * @return safe download filename
     */
    String getDownloadName();
}
