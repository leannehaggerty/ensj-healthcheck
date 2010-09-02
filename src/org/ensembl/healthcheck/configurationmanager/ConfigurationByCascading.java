package org.ensembl.healthcheck.configurationmanager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.healthcheck.ConfigurableTestRunner;

import uk.co.flamingpenguin.jewel.cli.OptionNotPresentException;


/**
 * <p>
 * Configuration class that gets its information from a List of other 
 * configuration objects. When a configuration object of type 
 * ConfigurationByCascading is asked for a parameter, it will iterate
 * through its list of configuration objects and return the value
 * it gets from the first configuration object that can provide one. 
 * </p>
 * 
 * <p>
 * Using this one can create a configuration system where one source 
 * overrides another like command line parameters overriding parameters
 * from a property file or a property file overriding default settings in
 * another property file or a combination of all of those.
 * </p>
 * 
 * <p>
 * This object could implement the 
 * AbstractAliasAwareWithStanardInvocationHanderConfigurationBacking
 * interface, but it doesn't seem necessary, since the individual objects
 * are already aware of aliases.
 * </p>
 *
 * @param <T>
 */
public class ConfigurationByCascading<T> extends AbstractConfigurationBacking {
	
	static final Logger log = Logger.getLogger(ConfigurationByCascading.class.getCanonicalName());
	
	private List<T> configurationObjects;

	// Awesome for debugging
	private final String type = "My type is ConfigurationByCascading.";

	/**
	 * @param configurationObjects
	 * 
	 * A list of configuration objects which will be used for the cascading 
	 * configuration.
	 * 
	 */
	public ConfigurationByCascading(List<T> configurationObjects) {
		this.configurationObjects = configurationObjects;
	}
	/**
	 * @param configurationObjects
	 * 
	 * An array of configuration objects which will be used for the cascading 
	 * configuration.
	 * 
	 */
	public ConfigurationByCascading(T[] configurationObjects) {
		
		ArrayList<T> l = new ArrayList<T>();		
		for (T c : configurationObjects) {
			l.add(c);
		}
		this.configurationObjects = l;
	}

	/** 
	 * Creates a summary of the configuration objects used, calls toString()
	 * recursively on each of them so the user has an idea of what the 
	 * configuration settings are.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 */
	public String toString() {
		
		StringBuffer toString = new StringBuffer(type + " comprising of: ");
		
		int order = 1;
		
		for (T confObj : configurationObjects) {
			toString.append("\n" + order + ".\n");
			toString.append(confObj.toString());
			order++;
		}		
		return toString.toString();
	}
	
	/**
	 * 
	 * This is called, when the user calls any of the get* or is* methods from
	 * the configuration interface. The calls are delegated to the 
	 * configuration objects which are stored in the configurationObjects 
	 * instance variable.
	 * 
	 */
	public Object invoke(Object proxy, Method m, Object[] args) throws OptionNotPresentException {
		
		Object result = null;
		
		String     methodName     = m.getName();
		Class<?>[] parameterTypes = m.getParameterTypes();

		if (methodName.equals("toString")) { return toString(); }
		
		boolean methodIsGetMethod = isGetMethod (m);
		boolean methodIsIsMethod  = isIsMethod  (m);
		
		try {
			// Search in all configuration object in the order they were given
			for (T configuration : configurationObjects) {
				
				Method methodInConfigObject = configuration.getClass().getMethod(methodName, parameterTypes);

				try {
					// Try to invoke the method the user wants on the current
					// configuration object. If it fails, because the parameter
					// was not present in this object, the configuration object
					// will throw a OptionNotPresentException which is wrapped
					// in an InvocationTargetException.
					//
					result = methodInConfigObject.invoke(configuration);
					
					if (methodIsGetMethod) {
						// If we are here, all went well and we are returning
						// the value of the parameter in the first 
						// configuration object in which it was set.
						//
						return result;
					}
					if (methodIsIsMethod) {
						// If we are here, the method called is a method
						// indicating if a specific parameter was set in the
						// configuration. If the current configuration returns
						// false, because it has not been set there, it might 
						// still have been set in one of the other 
						// configuration objects.
						//
						boolean boolResult = (Boolean) result;
						
						if ( boolResult ) {
							// If found we know it is there
							return true;
						}
						// Otherwise continue for loop with other configuration 
						// objects
					}
				} 
				// The next two exceptions should never occur.
				catch (IllegalArgumentException  e) { throw new RuntimeException(e); } 
				catch (IllegalAccessException    e) { throw new RuntimeException(e); }
				catch (InvocationTargetException e) { 
					if (e.getTargetException() instanceof OptionNotPresentException) {
						// In this case the variable was not found in the 
						// current configuration object, so proceed with next one.
					} else {
						if (e.getTargetException() instanceof UnsupportedOperationException) {
							
							UnsupportedOperationException myE = (UnsupportedOperationException) e.getTargetException();
							
							String errMsg = myE.getMessage() + "\n"
								+ "This error probably occurrs because the "
								+ "program is trying to access a method that "
								+ "you have not annotated with @Option in the "
								+ "configuration interface.\n";
 
							
							log.log(java.util.logging.Level.SEVERE, errMsg, e);
							
							throw new RuntimeException(errMsg, e);
							
						} else {
							// In any other case we have a problem.
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		// These two should never happen as well, so throw an error.
		catch (SecurityException     e) { throw new RuntimeException(e); }
		catch (NoSuchMethodException e) { throw new RuntimeException(e); }

		if (methodIsIsMethod) {
			// If the method is an is ismethod, then we have searched through 
			// all configurations and not found any in which the parameter was
			// true. Therefore false is returned.
			//
			return false;
		}

		// This would happen, if the user requests a configuration variable 
		// that has not been set in any of the configuration objects.
		throw new OptionNotPresentException(
			"Could not find configuration " 
			+ getVariableRequested(m) 
			+ " in any of the configuration objects.\n\n"
			+ toString()
		);			
	}		
}

