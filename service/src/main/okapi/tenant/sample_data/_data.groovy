import grails.gorm.multitenancy.Tenants
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import grails.databinding.SimpleMapDataBindingSource
import static grails.async.Promises.*
import com.k_int.web.toolkit.settings.AppSetting


log.info 'Importing sample data'

AppSetting test_app_setting = AppSetting.findByKey('test_app_setting') ?: new AppSetting(
                                  section:'test',
                                  settingType:'String',
                                  key: 'test_app_setting',
                                  ).save(flush:true, failOnError: true);


def cp_test = ensureTextProperty('test', false);
println("\n\n***Completed tenant setup***");
