import grails.gorm.multitenancy.Tenants
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import org.olf.okapi.modules.directory.Service
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import org.olf.okapi.modules.directory.NamingAuthority;
import grails.databinding.SimpleMapDataBindingSource
import static grails.async.Promises.*
import org.olf.reshare.FoafService
import com.k_int.web.toolkit.settings.AppSetting


CustomPropertyDefinition ensureTextProperty(String name, boolean local = true, String label = null) {
  CustomPropertyDefinition result = CustomPropertyDefinition.findByName(name) ?: new CustomPropertyDefinition(
                                        name:name,
                                        type:com.k_int.web.toolkit.custprops.types.CustomPropertyText.class,
                                        defaultInternal: local,
                                        label:label
                                      ).save(flush:true, failOnError:true);
  return result;
}

CustomPropertyDefinition ensureRefdataProperty(String name, boolean local, String category, String label = null) {

  CustomPropertyDefinition result = null;
  def rdc = RefdataCategory.findByDesc(category);

  if ( rdc != null ) {
    result = CustomPropertyDefinition.findByName(name) 
    if ( result == null ) {
      result = new CustomPropertyRefdataDefinition(
                                        name:name,
                                        defaultInternal: local,
                                        label:label,
                                        category: rdc)
      // Not entirely sure why type can't be set in the above, but other bootstrap scripts do this
      // the same way, so copying. Type doesn't work when set as a part of the definition above
      result.type=com.k_int.web.toolkit.custprops.types.CustomPropertyRefdata.class
      result.save(flush:true, failOnError:true);
    }
  }
  else {
    println("Unable to find category ${category}");
  }
  return result;
}


Service ensureService(String name, String type, List<String>tags, String address, Map custProps) {

  def result = Service.findByName(name);

  if ( result == null ) {

    // Until we figure out a good way to inject the databinder, lets do this the old way
    result = new org.olf.okapi.modules.directory.Service(
      name: name,
      type: RefdataValue.lookupOrCreate('Service.Type', 'ISO18626'),
      address: address
    )

    // This is an experiement - lets call the databinder so we can exploit all the clever functions that REST clients 
    // use like the auto-lookup of refdata and the resolution of custprops property names.
    //result = new org.olf.okapi.modules.directory.Service()
    // Map service_props = [
    //   name: name,
    //   type: type,
    //   address: address,
    //   tags: tags,
    //   customProperties: custProps
    // ]
    // grailsWebDataBinder.bind result, service_props as SimpleMapDataBindingSource
    result.save(flush:true, failOnError:true);
  }

  return result;
}

log.info 'Importing sample data'

AppSetting directory_announce_url = AppSetting.findByKey('directory_announce_url') ?: new AppSetting(
                                  section:'directory',
                                  settingType:'String',
                                  key: 'directory_announce_url',
                                  ).save(flush:true, failOnError: true);


/* if (existing_tenant) {
  log.info 'Skipping exisiting tenant'
  return
} */

def cp_ns = ensureTextProperty('ILLPreferredNamespaces', false);
def cp_url = ensureTextProperty('url', false);
def cp_demoprop = ensureTextProperty('demoCustprop', false);
def cp_test_prop = ensureTextProperty('TestParam', false);
def cp_z3950_base_name = ensureTextProperty('Z3950BaseName', false);
def cp_local_institutionalPatronId = ensureTextProperty('local_institutionalPatronId', true, label='Institutional patron ID');
def cp_local_widget2 = ensureTextProperty('local_widget_2', true, label='Widget 2');
def cp_local_widget3 = ensureTextProperty('local_widget_3', true, label='Widget 3');
def cp_local_alma_agency = ensureTextProperty('ALMA_AGENCY_ID', true, label='ALMA Agency ID');

NamingAuthority reshare = NamingAuthority.findBySymbol('RESHARE') ?: new NamingAuthority(symbol:'RESHARE').save(flush:true, failOnError:true);
NamingAuthority isil = NamingAuthority.findBySymbol('ISIL') ?: new NamingAuthority(symbol:'ISIL').save(flush:true, failOnError:true);
NamingAuthority oclc = NamingAuthority.findBySymbol('OCLC') ?: new NamingAuthority(symbol:'OCLC').save(flush:true, failOnError:true);
NamingAuthority exl = NamingAuthority.findBySymbol('EXL') ?: new NamingAuthority(symbol:'EXL').save(flush:true, failOnError:true);
NamingAuthority palci = NamingAuthority.findBySymbol('PALCI') ?: new NamingAuthority(symbol:'PALCI').save(flush:true, failOnError:true);
NamingAuthority cardinal = NamingAuthority.findBySymbol('CARDINAL') ?: new NamingAuthority(symbol:'CARDINAL').save(flush:true, failOnError:true);

RefdataValue.lookupOrCreate('Service.Type', 'ISO18626')
RefdataValue.lookupOrCreate('Service.Type', 'RTAC')
RefdataValue.lookupOrCreate('Service.Type', 'HTTP')
RefdataValue.lookupOrCreate('Service.Type', 'NCIP')
RefdataValue.lookupOrCreate('Service.Type', 'OAI-PMH')
RefdataValue.lookupOrCreate('Service.Type', 'Z3950')

RefdataValue.lookupOrCreate('Service.BusinessFunction', 'ILL')
RefdataValue.lookupOrCreate('Service.BusinessFunction', 'CIRC')
RefdataValue.lookupOrCreate('Service.BusinessFunction', 'RTAC')
RefdataValue.lookupOrCreate('Service.BusinessFunction', 'HARVEST')
RefdataValue.lookupOrCreate('Service.BusinessFunction', 'RS_STATS')

RefdataValue.lookupOrCreate('YNO', 'Yes')
RefdataValue.lookupOrCreate('YNO', 'No')
RefdataValue.lookupOrCreate('YNO', 'Other')

RefdataValue.lookupOrCreate('LoanPolicy', 'Lending all types')
RefdataValue.lookupOrCreate('LoanPolicy', 'Not Lending')
RefdataValue.lookupOrCreate('LoanPolicy', 'Lendin Physical only')
RefdataValue.lookupOrCreate('LoanPolicy', 'Lending Electronic only')

def cp_accept_returns_policy = ensureRefdataProperty('policy.ill.returns', false, 'YNO', 'Accept Returns' )
def cp_physical_loan_policy = ensureRefdataProperty('policy.ill.loan_policy', false, 'LoanPolicy', 'ILL Loan Policy' )
def cp_last_resort_policy = ensureRefdataProperty('policy.ill.last_resort', false, 'YNO', 'Consider Institution As Last Resort' )
def cp_lb_ratio = ensureTextProperty('policy.ill.InstitutionalLoanToBorrowRatio', true, label='ILL Loan To Borrow Ratio');

def na_reshare = NamingAuthority.findBySymbol('RESHARE') ?: new NamingAuthority(symbol:'RESHARE').save(flush:true, failOnError:true);
def na_isil = NamingAuthority.findBySymbol('ISIL') ?: new NamingAuthority(symbol:'ISIL').save(flush:true, failOnError:true);
def na_palci = NamingAuthority.findBySymbol('PALCI') ?: new NamingAuthority(symbol:'PALCI').save(flush:true, failOnError:true);

// def iso_18626_loopback_service = ensureService('loopback-iso-18626',
//                                                        'ISO18626',
//                                                        ['system-default'],
//                                                        'http://localhost:9130/rs/iso18626',
//                                                        null);
println("\n\n***Completed tenant setup***");
