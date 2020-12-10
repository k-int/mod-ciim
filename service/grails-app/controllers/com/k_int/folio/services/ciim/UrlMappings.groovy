package org.olf

class UrlMappings {

  static mappings = {
    "/"(controller: 'application', action:'index')

    "/ciim/settings/appSettings" (resources: 'setting');
  }
}
