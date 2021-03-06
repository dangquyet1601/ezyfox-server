package com.tvd12.ezyfoxserver.support.entry;

import java.util.List;

import com.tvd12.ezyfox.bean.EzyBeanContext;
import com.tvd12.ezyfox.bean.EzyBeanContextBuilder;
import com.tvd12.ezyfox.binding.EzyBindingContext;
import com.tvd12.ezyfox.binding.EzyBindingContextBuilder;
import com.tvd12.ezyfox.binding.EzyMarshaller;
import com.tvd12.ezyfox.binding.EzyUnmarshaller;
import com.tvd12.ezyfox.binding.impl.EzySimpleBindingContext;
import com.tvd12.ezyfox.core.annotation.EzyServerEventHandler;
import com.tvd12.ezyfoxserver.app.EzyAppRequestController;
import com.tvd12.ezyfoxserver.command.EzyAppSetup;
import com.tvd12.ezyfoxserver.command.EzySetup;
import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.controller.EzyEventController;
import com.tvd12.ezyfoxserver.ext.EzyAbstractAppEntry;
import com.tvd12.ezyfoxserver.support.controller.EzyUserRequestAppPrototypeController;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class EzySimpleAppEntry extends EzyAbstractAppEntry {

	@Override
	public final void config(EzyAppContext context) {
		preConfig(context);
		EzyBeanContext beanContext = createBeanContext(context);
		addEventControllers(context, beanContext);
		setAppRequestController(context, beanContext);
		postConfig(context);
	}
	
	protected void preConfig(EzyAppContext context) {}
	protected void postConfig(EzyAppContext context) {}
	
	private void addEventControllers(EzyAppContext appContext, EzyBeanContext beanContext) {
		EzySetup setup = appContext.get(EzySetup.class);
		List<Object> eventControllers = beanContext.getSingletons(EzyServerEventHandler.class);
		for (Object controller : eventControllers) {
			Class<?> controllerType = controller.getClass();
			EzyServerEventHandler annotation = controllerType.getAnnotation(EzyServerEventHandler.class);
			EzyEventType eventType = EzyEventType.valueOf(annotation.event());
			setup.addEventController(eventType, (EzyEventController) controller);
			getLogger().info("add  event {} controller {}", eventType, controller);
		}
	}
	
	private void setAppRequestController(EzyAppContext appContext, EzyBeanContext beanContext) {
		EzyAppSetup setup = appContext.get(EzyAppSetup.class);
		EzyAppRequestController controller = newUserRequestController(beanContext);
		setup.setRequestController(controller);
	}
	
	protected EzyAppRequestController newUserRequestController(EzyBeanContext beanContext) {
		return EzyUserRequestAppPrototypeController.builder()
				.beanContext(beanContext)
				.build();
	}

	protected EzyBeanContext createBeanContext(EzyAppContext context) {
		EzyBindingContext bindingContext = createBindingContext();
		EzyMarshaller marshaller = bindingContext.newMarshaller();
		EzyUnmarshaller unmarshaller = bindingContext.newUnmarshaller();
		EzyBeanContextBuilder beanContextBuilder = EzyBeanContext.builder()
				.addSingleton("appContext", context)
				.addSingleton("marshaller", marshaller)
				.addSingleton("unmarshaller", unmarshaller)
				.addSingleton("zoneContext", context.getParent())
				.addSingleton("serverContext", context.getParent().getParent())
				.addSingleton("userManager", context.getApp().getUserManager());
		Class[] singletonClasses = getSingletonClasses();
		for(Class singletonClass : singletonClasses)
			beanContextBuilder.addSingletonClass(singletonClass);
		String[] scanablePackages = getScanableBeanPackages();
		for(String scanablePackage : scanablePackages)
			beanContextBuilder.scan(scanablePackage);
		setupBeanContext(context, beanContextBuilder);
		return beanContextBuilder.build();
	}
	
	protected EzyBindingContext createBindingContext() {
		EzyBindingContextBuilder builder = EzyBindingContext.builder();
		String[] scanablePackages = getScanableBindingPackages();
		for(String pack : scanablePackages)
			builder.scan(pack);
		EzySimpleBindingContext answer = builder.build();
		return answer;
	}
	
	protected Class[] getSingletonClasses() {
		return new Class[0];
	}
	
	protected abstract String[] getScanableBeanPackages();
	protected abstract String[] getScanableBindingPackages();
	protected abstract void setupBeanContext(EzyAppContext context, EzyBeanContextBuilder builder);

	@Override
	public void start() throws Exception {}

	@Override
	public void destroy() {}
}
