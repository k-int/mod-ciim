package com.k_int.folio.services.ciim

class UrlMappings {

  static mappings = {
    "/"(controller: 'application', action:'index');

    "/ciim/settings/appSettings" (resources: 'setting');

    // Call /ciim/refdata to list all refdata categories
    '/ciim/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }
  }
}
