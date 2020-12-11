package com.k_int.folio.services.ciim

class UrlMappings {

  static mappings = {
    "/"(controller: 'application', action:'index');

    "/ciim/settings/appSettings" (resources: 'setting');
    "/ciim/wibble" (controller: 'setting', action: 'wibble');
  }
}
