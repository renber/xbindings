package org.jdesktop.xbindings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.PropertyHelper;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;

/**
 * A class which groups bindings and allows to unbind them all at once
 * @author renber
 */
public class BindingContext implements XBinding {

    // the registered bindings
    private List<WeakReference<XBinding>> registeredBindings = new ArrayList<WeakReference<XBinding>>();

    private boolean autoBind;
    
    /**
     * Creates a binding context with enabled auto binding
     */
    public BindingContext() {
    	this(true);
    }
    
    /**
     * Creates a binding context
     * @autoBind Whether auto binding should enabled or disabled
     */
    public BindingContext(boolean autoBind) {
    	this.autoBind = autoBind;
    }

    /**
     * If enabled, Bindings which are registered or are created with bind are bound automatically
     */
    public void setAutoBind(boolean newValue) {
    	autoBind = newValue;
    }

    /**
     * If enabled, Bindings created with bind are bound automatically
     */
    public boolean isAutoBind() {
    	return autoBind;
    }

    /**
     * Register the given binding with this BindingContext
     * @param binding
     */
    public void register(XBinding binding) {

        for(WeakReference<XBinding> wr: registeredBindings) {
            if (wr.get() == binding) {
                return; // binding already in list
            }
        }
        
        if (isAutoBind() && !binding.isBound())
        	binding.bind();

        registeredBindings.add(new WeakReference<XBinding>(binding));
    }

     /**
     * Binds all registered bindings
     */
    @Override
    public void bind() {
        for(WeakReference<XBinding> wr: registeredBindings) {
            if (wr.get() != null && !wr.get().isBound()) {
                wr.get().bind();
            }
        }
    }

    /**
     * Unbind all registered bindings
     */
    @Override
    public void unbind() {
        for(WeakReference<XBinding> wr: registeredBindings) {
            if (wr.get() != null && wr.get().isBound()) {
                wr.get().unbind();
            }
        }
    }

    /**
     * Unbind and remove all registered bindings from this binding context
     */
    public void clear() {
    	unbind();
    	registeredBindings.clear();
    }
    
    /**
     * create a binding (and bind it immediately if autoBind is set)
     * @param sourceDataContext The source data context
     * @param targetDataContext The target data context 
     * @return
     */
    public AutoBinding bind(DataContext sourceDataContext, DataContext targetDataContext) {
    	if (!(sourceDataContext instanceof BeansDataContext) || !(targetDataContext instanceof BeansDataContext)) {
    		throw new IllegalArgumentException("Only BeansDataContext is supported");
    	}
    	
    	return bind( ((BeansDataContext)sourceDataContext).getSource(), ((BeansDataContext)sourceDataContext).getPropertyHelper(), ((BeansDataContext)targetDataContext).getSource(), ((BeansDataContext)targetDataContext).getPropertyHelper(), AutoBinding.UpdateStrategy.READ, null);
    }
    
    /**
     * create a binding (and bind it immediately if autoBind is set)
     * @param sourceDataContext The source data context
     * @param target The target object 
     * @param propertyPath The property of target to bind to
     * @return
     */
    public AutoBinding bind(DataContext sourceDataContext, Object target, String propertyPath) {
    	if (!(sourceDataContext instanceof BeansDataContext)) {
    		throw new IllegalArgumentException("Only BeansDataContext is supported");
    	}
    	
    	return bind(sourceDataContext, target, propertyPath, AutoBinding.UpdateStrategy.READ, null);
    }
    
    /**
     * create a binding (and bind it immediately if autoBind is set)
     * @param sourceDataContext The source data context
     * @param target The target object 
     * @param propertyPath The property of target to bind to
     * @param converter The binding value converter
     * @return
     */
    public AutoBinding bind(DataContext sourceDataContext, Object target, String propertyPath,  Converter converter) {
    	if (!(sourceDataContext instanceof BeansDataContext)) {
    		throw new IllegalArgumentException("Only BeansDataContext is supported");
    	}
    	
    	return bind(sourceDataContext, target, propertyPath, AutoBinding.UpdateStrategy.READ, converter);
    }    
    
    /**
     * create a binding (and bind it immediately if autoBind is set)
     * @param sourceDataContext The source data context
     * @param target The target object 
     * @param propertyPath The property of target to bind to
     * @param strategy The update strategy of the binding
     * @return
     */
    public AutoBinding bind(DataContext sourceDataContext, Object target, String propertyPath, AutoBinding.UpdateStrategy strategy) {
    	if (!(sourceDataContext instanceof BeansDataContext)) {
    		throw new IllegalArgumentException("Only BeansDataContext is supported");
    	}
    	
    	return bind(sourceDataContext, target, propertyPath, strategy, null);
    }    
    
    /**
     * create a binding (and bind it immediately if autoBind is set)
     * @param sourceDataContext The source data context
     * @param target The target object 
     * @param propertyPath The property of target to bind to
     * @param strategy The update strategy of the binding
     * @param converter The binding value converter
     * @return
     */
    public AutoBinding bind(DataContext sourceDataContext, Object target, String propertyPath, AutoBinding.UpdateStrategy strategy, Converter converter) {
    	if (!(sourceDataContext instanceof BeansDataContext)) {
    		throw new IllegalArgumentException("Only BeansDataContext is supported");
    	}
    	
    	return bind( ((BeansDataContext)sourceDataContext).getSource(), ((BeansDataContext)sourceDataContext).getPropertyHelper(), target, BeanProperty.create(propertyPath), strategy, converter);
    }      

    /**
     * create a binding (and bind it immediately if autoBind is set)
     *
     * @param source The Object to bind "from"
     * @param sourceProperty The Property to bind "from"
     * @param target The Object to bind "to"
     * @param targetProperty The Property to bind "to"
     * @param strategy The updating strategy to use with the binding
     * @param converter The converter to associate with the binding or null
     * @return
     */
    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, PropertyHelper targetProperty, AutoBinding.UpdateStrategy strategy, Converter converter) {
        AutoBinding bnd = Bindings.createAutoBinding(strategy, source, sourceProperty, target, targetProperty);

        if (converter != null) {
            bnd.setConverter(converter);
        }

        // register the binding
        register(bnd);

        return bnd;
    }

    // useful overloads
    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, PropertyHelper targetProperty, Converter conv) {
        return bind(source, sourceProperty, target, targetProperty, AutoBinding.UpdateStrategy.READ, conv);
    }

    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, PropertyHelper targetProperty) {
        return bind(source, sourceProperty, target, targetProperty, AutoBinding.UpdateStrategy.READ, null);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, PropertyHelper targetProperty) {
        return bind(source, BeanProperty.create(sourceProperty), target, targetProperty, AutoBinding.UpdateStrategy.READ, null);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, String targetProperty) {
        return bind(source, sourceProperty, target, targetProperty, AutoBinding.UpdateStrategy.READ, null);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, String targetProperty, Converter converter) {
        return bind(source, BeanProperty.create(sourceProperty), target, BeanProperty.create(targetProperty), AutoBinding.UpdateStrategy.READ, converter);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, PropertyHelper targetProperty, Converter converter) {
        return bind(source, BeanProperty.create(sourceProperty), target, targetProperty, AutoBinding.UpdateStrategy.READ, converter);
    }

    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, String targetProperty) {
        return bind(source, sourceProperty, target, BeanProperty.create(targetProperty), AutoBinding.UpdateStrategy.READ, null);
    }

    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, String targetProperty, Converter converter) {
        return bind(source, sourceProperty, target, BeanProperty.create(targetProperty), AutoBinding.UpdateStrategy.READ, converter);
    }

    // with update strategy
    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, PropertyHelper targetProperty, AutoBinding.UpdateStrategy strategy) {
        return bind(source, sourceProperty, target, targetProperty, strategy, null);
    }

    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, String targetProperty, AutoBinding.UpdateStrategy strategy) {
        return bind(source, sourceProperty, target, BeanProperty.create(targetProperty), strategy, null);
    }

    public AutoBinding bind(Object source, PropertyHelper sourceProperty, Object target, String targetProperty, AutoBinding.UpdateStrategy strategy, Converter converter) {
        return bind(source, sourceProperty, target, BeanProperty.create(targetProperty), strategy, converter);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, PropertyHelper targetProperty, AutoBinding.UpdateStrategy strategy) {
        return bind(source, BeanProperty.create(sourceProperty), target, targetProperty, strategy, null);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, String targetProperty, AutoBinding.UpdateStrategy strategy) {
        return bind(source, sourceProperty, target, targetProperty, strategy, null);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, String targetProperty, AutoBinding.UpdateStrategy strategy, Converter converter) {
        return bind(source, BeanProperty.create(sourceProperty), target, BeanProperty.create(targetProperty), strategy, converter);
    }

    public AutoBinding bind(Object source, String sourceProperty, Object target, PropertyHelper targetProperty, AutoBinding.UpdateStrategy strategy, Converter converter) {
        return bind(source, BeanProperty.create(sourceProperty), target, targetProperty, strategy, converter);
    }

    /**
     * Returns true, when all registered bindings are bound
     * otherwise false
     */
	@Override
	public boolean isBound() {
		for(WeakReference<XBinding> bnds: registeredBindings) {
			if (bnds.get() != null && bnds.get().isBound())
				return false;
		}
		
		return true;
	}

}
