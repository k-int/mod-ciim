package org.olf

class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')

    "/directory/api/findSymbol"(controller: 'application', action:'findSymbol')
    "/directory/api/addFriend"(controller: 'application', action:'addFriend')
    "/directory/api/freshen"(controller: 'application', action:'freshen')

    "/directory/entry"(resources:'directoryEntry')
    "/directory/symbol"(resources:'Symbol')
    "/directory/service"(resources:'Service')
    "/directory/serviceAccount"(resources:'ServiceAccount')
    "/directory/namingAuthority"(resources:'NamingAuthority')

    "/directory/kiwt/config/$extended?" (controller: 'directoryConfig' , action: "resources")
    "/directory/kiwt/config/schema/$type" (controller: 'directoryConfig' , action: "schema")
    "/directory/kiwt/config/schema/embedded/$type" (controller: 'directoryConfig' , action: "schemaEmbedded")
    "/directory/kiwt/raml" (controller: 'directoryConfig' , action: "raml")

    "/directory/settings/worker" (controller: 'directorySettings', action: 'worker');
    "/directory/settings/foaf" (controller: 'directorySettings', action: 'foaf');

    "/directory/externalApi/entry/$slug" (controller: 'externalApi', action:'directoryEntry', excludes: ['update', 'patch', 'save', 'create', 'edit', 'delete'] ) 

    // Call /rs/refdata to list all refdata categories
    '/directory/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }

    // Call /rs/custprop  to list all custom properties
    '/directory/custprops'(resources: 'customPropertyDefinition')
    '/directory/tags'(resources: 'tags')

    "500"(view: '/error')
    "404"(view: '/notFound')


  }
}
