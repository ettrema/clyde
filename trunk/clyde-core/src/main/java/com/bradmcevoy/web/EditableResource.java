package com.bradmcevoy.web;

import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Resource;

public interface EditableResource extends Resource {
    PostableResource getEditPage();
}
