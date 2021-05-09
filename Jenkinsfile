@Library ('kifolio') _

// Build and CD this module - publish the docker image, provision this snapshot version in the cluster and then update the named
// tenants to use this snapshot.
Map args=[
  tenantsToUpdate:['kint1', 'kiuat1'],
  dockerImageName:'knowledgeintegration/mod-ciim',
  serviceName:'mod-ciim',
  descriptorRegistries:['http://okapi.reshare:9130/_/proxy/modules'
  ],
  deploymentTemplate:'scripts/k8s_deployment_template.yaml',
  targetNamespace:'reshare'
]
buildOkapiModule(args)
