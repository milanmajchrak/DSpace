/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.builder;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.requestitem.RequestItem;
import org.dspace.app.requestitem.service.RequestItemService;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.core.Context;

/**
 * Manage the creation and cleanup of {@link RequestItem}s for testing.
 *
 * @author Mark H. Wood <mwood@iupui.edu>
 */
public class RequestItemBuilder
        extends AbstractBuilder<RequestItem, RequestItemService> {
    private static final Logger LOG = LogManager.getLogger();

    public static final String REQ_EMAIL = "jsmith@example.com";
    public static final String REQ_NAME = "John Smith";
    public static final String REQ_MESSAGE = "Please send me a copy of this.";
    public static final String REQ_PATH = "test/file";

    private RequestItem requestItem;

    protected RequestItemBuilder(Context context) {
        super(context);
    }

    @Override
    public void cleanup()
            throws Exception {
        LOG.debug("cleanup()");
        if (null != requestItem) {
            delete(context, requestItem);
            requestItem = null;
        } else {
            LOG.debug("nothing to clean up.");
        }
    }

    public static RequestItemBuilder createRequestItem(Context ctx, Item item,
            Bitstream bitstream) {
        RequestItemBuilder builder = new RequestItemBuilder(ctx);
        return builder.create(item, bitstream);
    }

    private RequestItemBuilder create(Item item, Bitstream bitstream) {
        String token;
        try {
            token = requestItemService.createRequest(context, bitstream,
                    item, true, REQ_EMAIL, REQ_NAME, REQ_MESSAGE);
        } catch (SQLException ex) {
            return handleException(ex);
        }
        requestItem = requestItemService.findByToken(context, token);
        requestItem.setAccept_request(false);
        requestItemService.update(context, requestItem);
        return this;
    }

    @Override
    public RequestItem build() {
        // Nothing to build.
        return requestItem;
    }

    @Override
    public void delete(Context context, RequestItem request)
            throws Exception {
        requestItemService.delete(context, request);
    }

    /**
     * Delete a request identified by its token.  If no such token is known,
     * simply return.
     * @param token the token identifying the request.
     */
    static public void deleteRequestItem(String token) {
        try (Context context = new Context()) {
            RequestItem request = requestItemService.findByToken(context, token);
            requestItemService.delete(context, request);
        }
    }

    @Override
    protected RequestItemService getService() {
        return requestItemService;
    }
}
