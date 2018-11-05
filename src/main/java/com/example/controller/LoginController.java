package com.example.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.example.model.*;
import com.example.repository.TargetManagerRepository;
import com.example.service.FileService;
import com.example.service.LocationService;
import com.example.service.VuforiaService;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

	@Resource(name = "vuforiaService")
	VuforiaService vuforiaService;

	@Resource(name = "locationService")
	LocationService locationService;

	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	private UserService userService;

	@Qualifier("targetManagerRepository")
	@Autowired
	private TargetManagerRepository targetManagerRepository;

	@RequestMapping(value={"/", "/login"}, method = RequestMethod.GET)
	public ModelAndView login(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		return modelAndView;
	}

	@RequestMapping(value="/firstRegistration", method = RequestMethod.GET)
	public ModelAndView firstRegistration(){
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.setViewName("firstRegistration");
		return modelAndView;
	}

	@RequestMapping(value="/registration", method = RequestMethod.GET)
	public ModelAndView registration(){
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.setViewName("registration");
		return modelAndView;
	}

	@RequestMapping(value="/locationAll", method = RequestMethod.GET,produces="application/json")
	@ResponseBody
	public String getAllLocation(@RequestParam(value = "long", defaultValue="0") String longitud, @RequestParam(value = "lat", defaultValue="0") String latitud){
		if(!longitud.equals("0") && !latitud.equals("0")){
			Location location = locationService.findLocationByLongAndLat(longitud,latitud);
			JsonObject locationJson = new JsonObject();
			locationJson.addProperty("descripcion", location.getNombdescripcionre());
			if(!location.getTargetManagers().isEmpty()){
				locationJson.addProperty("videoLocation",location.getTargetManagers().get(0).getVideoUrl());
			}
			return locationJson.toString();
		}else{
			List<Location> locations = locationService.findLocationAll();
			List<JsonObject> locationreturn = new ArrayList<>();
			for (Location locate : locations){
				JsonObject location = new JsonObject();
				location.addProperty("latitud", locate.getLatitud());
				location.addProperty("longitud", locate.getLongitud());
				location.addProperty("nombre", locate.getNombre());
				locationreturn.add(location);
			}
			JsonObject person = new JsonObject();
			locationreturn.add(person);

			return locationreturn.toString();
		}
	}
	
	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult,@RequestParam(value = "i", required=false) Integer i) {
		ModelAndView modelAndView = new ModelAndView();
		User userExists = userService.findUserByEmail(user.getEmail());
		if (userExists != null) {
			bindingResult
					.rejectValue("email", "error.user",
							"There is already a user registered with the email provided");
		}
		if (bindingResult.hasErrors()) {
			if(i != null){
				modelAndView.addObject("errorMessage", "El mail ingresado ya existe en el sistema. Intente nuevamente");
				modelAndView.setViewName("firstRegistration");
			}else{
				modelAndView.setViewName("registration");
			}
		} else {
			userService.saveUser(user);
			modelAndView.addObject("successMessage", "El usuario fue registrado exitosamente.");
			modelAndView.addObject("user", new User());
			if(i != null){
				modelAndView.setViewName("firstRegistration");
			}else{
				modelAndView.setViewName("registration");
			}
			
		}
		return modelAndView;
	}

	@RequestMapping(value = "/editUser", method = RequestMethod.POST)
	public ModelAndView editUser(@Valid User user, BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView();
		User userExists = userService.findUserById(user.getId());
		if (userExists != null) {
			userService.updateUser(user);
		}
		if (bindingResult.hasErrors()) {
			//modelAndView.setViewName("registration");

		}
		return new ModelAndView("redirect:admin/user");
	}
	
	@RequestMapping(value="/admin/home", method = RequestMethod.GET)
	public ModelAndView home(){
		ModelAndView modelAndView = new ModelAndView();
		List<TargetManager> listTarget = targetManagerRepository.findAll();
		int cantRelation=0;
		for(TargetManager targets : listTarget){
			if(targets.getLocation() != null){
				cantRelation++;
			}
		}
		int porcentRelationTarget = cantRelation == 0 ? 0 : (cantRelation * 100)/listTarget.size();
		List<User> listUser = userService.findAllUser();
		List<Location> locations = locationService.getLocationAll();
		modelAndView.addObject("targets", listTarget);
		modelAndView.addObject("users", listUser);
		modelAndView.addObject("percentRelation", porcentRelationTarget);
		modelAndView.addObject("locations", locations);
		modelAndView.setViewName("admin/index");
		return modelAndView;
	}

	@RequestMapping(value="/admin/location/list", method = RequestMethod.GET)
	public ModelAndView locationList(){
		ModelAndView modelAndView = new ModelAndView();
		List<Location> locations = locationService.getLocationAll();
		modelAndView.addObject("locations", locations);
		modelAndView.setViewName("admin/listLocation");
		return modelAndView;
	}

	@GetMapping("/location/{id}/remove")
	public ModelAndView removeLocation(@PathVariable("id") int id){
		ModelAndView modelAndView = new ModelAndView();
		try{
			Location location = locationService.findLocationById(id);
			locationService.remove(location);
		}catch (Exception e){
			modelAndView.addObject("error", "No se puede eliminar, vuelva a intentarlo.");
		}
		modelAndView.setViewName("admin/listLocation");
		return modelAndView;
	}

	@GetMapping("/location/{id}/edit")
	public ModelAndView editLocation(@PathVariable("id") int id){
		ModelAndView modelAndView = new ModelAndView();
		try{
			Location location = locationService.findLocationById(id);
			modelAndView.addObject("location",location);
			modelAndView.setViewName("admin/location");
			return modelAndView;
		}catch (Exception e){
			modelAndView.addObject("error", "Hubo un problema al buscar la localidad,vualva a intentarlo.");
			modelAndView.setViewName("admin/listLocation");
			return modelAndView;
		}

	}

	@RequestMapping(value = "/relationLocation", method = RequestMethod.POST)
	public ModelAndView editUser( LocationView locationView,BindingResult bindingResult) {
		TargetManager target = fileService.getTarget(locationView.id);
		Location location = locationService.findLocationById(locationView.getLocationId());
		target.setLocation(location);
		targetManagerRepository.save(target);
		return new ModelAndView("redirect:admin/home");
	}

	@RequestMapping(value = "/location/edit", method = RequestMethod.POST)
	public ModelAndView editLocationPost(@Valid Location location, BindingResult bindingResult) {
		locationService.update(location);
		ModelAndView modelAndView = new ModelAndView();
		List<Location> locations = locationService.getLocationAll();
		modelAndView.addObject("locations", locations);
		modelAndView.setViewName("admin/listLocation");
		return modelAndView;
	}

	@RequestMapping(value="/admin/user", method = RequestMethod.GET)
	public ModelAndView user(){
		ModelAndView modelAndView = new ModelAndView();
		List<User> listUser = userService.findAllUser();
		modelAndView.addObject("users", listUser);
		modelAndView.setViewName("admin/user");
		return modelAndView;
	}

	@GetMapping("/user/{id}/remove")
	public ModelAndView removeUser(@PathVariable("id") int id){
		User user = userService.findUserById(id);
		userService.softDeleteUser(user);
		return new ModelAndView("redirect:admin/user");
	}

	@GetMapping("/user/{id}/edit")
	public ModelAndView editUser(@PathVariable("id") int id){
		ModelAndView modelAndView = new ModelAndView();
		User user = userService.findUserById(id);
		List<Role> roles = userService.finAllRoles();
		modelAndView.addObject("userEdit", user);
		modelAndView.addObject("roles", roles);
		modelAndView.setViewName("registration");
		return modelAndView;
	}


	@RequestMapping(value="/admin/target", method = RequestMethod.GET)
	public ModelAndView target(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/target");
		return modelAndView;
	}

	@RequestMapping(value="/admin/location", method = RequestMethod.GET)
	public ModelAndView location(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/location");
		return modelAndView;
	}

	@PostMapping("/target")
	public ModelAndView singleFileUpload(@RequestParam("fileImage") MultipartFile fileImage,@RequestParam("fileVideo") MultipartFile fileVideo,
								   @RequestParam("name") String name,RedirectAttributes redirectAttributes) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/target");
		String message;

		if (!fileImage.getContentType().contains("image")){
			message = "No se cargó un archivo de imagen.";
			modelAndView.addObject("fileEmpty", message);
			return modelAndView;
		}

		if (!fileVideo.getContentType().contains("video")){
			message = "No se cargó un archivo de video.";
			modelAndView.addObject("fileEmpty", message);
			return modelAndView;
		}

		if (fileImage.isEmpty() || fileVideo.isEmpty()) {
			message = "Falta cargar archivo de imagen o video.";
			modelAndView.addObject("fileEmpty", message);
			return modelAndView;
		}

		String pathImage = fileService.saveFile(fileImage, "image");
		String pathVideo = fileService.saveFile(fileVideo, "video");

		try {
			JSONObject response = vuforiaService.postTarget(name, pathImage, pathVideo);
			String resultCode = response.getString("result_code");
			if (resultCode.contains("Exist")){
				message = "El nombre ingresado ya se encuentra relacionado a un marcador, vuelva a intentarlo.";
				modelAndView.addObject("fileEmpty", message);
				return modelAndView;
			}else{
				String uniqueTargetId = response.has("target_id") ? response.getString("target_id") : "";
				fileService.saveData(uniqueTargetId,name, pathImage, pathVideo);
				message = "Se registró el target de forma correcta.";
				modelAndView.addObject("fileSuccess", message);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return modelAndView;
	}

	@PostMapping("/location")
	public ModelAndView saveLocation(@RequestParam("address") String lugar, @RequestParam("lat") String latitud,
									 @RequestParam("lng") String longitud, @RequestParam("descrip") String descrip){
		try{
			locationService.saveData(lugar,longitud,latitud,descrip);
			return new ModelAndView("redirect:admin/home");
		}catch (Exception e){
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.addObject("error", "Error al cargar el sitio, vuelva a intentar");
			modelAndView.setViewName("admin/location");
			return modelAndView;
		}
	}

	@GetMapping("/target/{id}")
	public ModelAndView verTarget(@PathVariable("id") int id){
		TargetManager target = fileService.getTarget(id);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/viewTarget");
		modelAndView.addObject("nombreMarcador", target.getName());
		modelAndView.addObject("marcadorFind", target);
		modelAndView.addObject("videoMarcador", target.getVideoUrl());
		String base64Image = "";
		File file = new File(target.getImage());
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			// Reading a Image file from file system
			byte imageData[] = new byte[(int) file.length()];
			imageInFile.read(imageData);
			base64Image = Base64.encodeBase64String(imageData);
			modelAndView.addObject("encodeImage", base64Image);
		} catch (FileNotFoundException e) {
			System.out.println("Image not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Image " + ioe);
		}
		return modelAndView;
	}

	@GetMapping("/target/{id}/edit")
	public ModelAndView editarTarget(@PathVariable("id") int id){
		TargetManager target = fileService.getTarget(id);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("marcadorEdit", target);
		modelAndView.setViewName("admin/target");
		return modelAndView;
	}

	@GetMapping("/target/{id}/relationship")
	public ModelAndView relationshipTarget(@PathVariable("id") int id){
		TargetManager target = fileService.getTarget(id);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/viewTarget");
		modelAndView.addObject("nombreMarcador", target.getName());
		modelAndView.addObject("target", target);
		modelAndView.addObject("videoMarcador", target.getVideoUrl());
		List<Location> lcoationAll = locationService.getLocationAll();
		modelAndView.addObject("lcoationAll", lcoationAll);
		LocationView locationView = new LocationView();
		modelAndView.addObject("locationView", locationView);
		String base64Image = "";
		File file = new File(target.getImage());
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			// Reading a Image file from file system
			byte imageData[] = new byte[(int) file.length()];
			imageInFile.read(imageData);
			base64Image = Base64.encodeBase64String(imageData);
			modelAndView.addObject("encodeImage", base64Image);
		} catch (FileNotFoundException e) {
			System.out.println("Image not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Image " + ioe);
		}
		return modelAndView;
	}

	@RequestMapping(value = "/editMarcador", method = RequestMethod.POST)
	public ModelAndView editMarcador(@Valid TargetManager target, BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView();
		try {
			JSONObject response = vuforiaService.updateTarget(target);
			if(response.getString("result_code").contains("Success")){
				TargetManager targetUpdate = fileService.getTarget(target.getId());
				targetUpdate.setName(target.getName());
				targetUpdate.setActive(target.isActive());
				targetManagerRepository.save(targetUpdate);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ModelAndView("redirect:admin/home");
	}

	@RequestMapping(value="/error", method = RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("error");
		return modelAndView;
	}

}
