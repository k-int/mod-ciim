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
import grails.databinding.SimpleMapDataBindingSource
import static grails.async.Promises.*
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

AppSetting test_app_setting = AppSetting.findByKey('test_app_setting') ?: new AppSetting(
                                  section:'test',
                                  settingType:'String',
                                  key: 'test_app_setting',
                                  ).save(flush:true, failOnError: true);


def cp_test = ensureTextProperty('test', false);
println("\n\n***Completed tenant setup***");
