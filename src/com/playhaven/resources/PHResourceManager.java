package com.playhaven.resources;

import java.util.HashMap;

import com.playhaven.resources.files.*;

/**
 * @class {@link PHResourceManager}
 * This class handles resource management. Currently, we simply store all resources as classes in byte arrays. Since
 * Android doesn't support resource loading from any folder, this is the simplest method (and the only one that works).
 * Everything is encoded as Base64 string and wrapped in a resource subclass. We register each resource when you call
 * loadResources. This class is a singleton.
 * 
 * Note: You are not allowed to post this to dailywtf.com. If you do, I *will* send someone. ;)
 * 
 * TODO: Ensure thread safe access to static resources
 * @author samuelstewart
 *
 */
public class PHResourceManager {
	private HashMap<String, PHResource> resources = null;
	
	private boolean hasLoaded = false;
	
	private static PHResourceManager res_manager = null;
	
	/** We statically load all resources here.*/
	private void registerResources() {
		resources = new HashMap<String, PHResource>();
		resources.put("close_inactive", new PHCloseImageResource());
		resources.put("close_active", new PHCloseActiveImageResource());
		resources.put("badge_image", new PHBadgeImageResource());
	}
	/** Singleton class only.*/
	private PHResourceManager() {
		//singleton only..
	}
	
	public static PHResourceManager sharedResourceManager() {
		if (res_manager == null) {
			res_manager = new PHResourceManager();
			res_manager.loadResources();
		}
		return res_manager;
	}
	
	/** Gets resource for specified key. Returns null if no value for key.*/
	public PHResource getResource(String key) {
		if (res_manager != null)
			return resources.get(key);
		return null;
	}
	
	/** Registers resource for key. If the key already exists, the previous resource is replaced.*/
	public void registerResource(String key, PHResource resource) {
		if (res_manager != null)
			resources.put(key, resource);
	}
	
	/** Call this to init the resources.*/
	private void loadResources() {
		if (resources == null && !hasLoaded) {
			registerResources();
			hasLoaded = true;
		}
	}
}
