package com.example.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.example.model.Role;
import com.example.model.TargetManager;
import com.example.repository.TargetManagerRepository;
import com.example.service.FileService;
import com.example.service.VuforiaService;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.model.User;
import com.example.service.UserService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Controller
public class LoginController {

	@Resource(name = "vuforiaService")
	VuforiaService vuforiaService;

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
		return new ModelAndView("redirect:/admin/user");
	}
	
	@RequestMapping(value="/admin/home", method = RequestMethod.GET)
	public ModelAndView home(){
		ModelAndView modelAndView = new ModelAndView();
		List<TargetManager> listTarget = targetManagerRepository.findAll();
		List<User> listUser = userService.findAllUser();
		modelAndView.addObject("targets", listTarget);
		modelAndView.addObject("users", listUser);
		modelAndView.setViewName("admin/index");
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
		return new ModelAndView("redirect:/admin/user");
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
		modelAndView.setViewName("/admin/target");
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
			String idTarget = vuforiaService.postTarget(name, pathImage, pathVideo);
			fileService.saveData(idTarget,name, pathImage, pathVideo);
			message = "Se registró el target de forma correcta.";
			modelAndView.addObject("fileSuccess", message);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return modelAndView;
	}

	@GetMapping("/target/{id}")
	public ModelAndView verTarget(@PathVariable("id") int id){
		TargetManager target = fileService.getTarget(id);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("/admin/viewTarget");

		/*String encodedfile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(target.getImage());
			byte[] bytes = new byte[(int)target.getImage().length()];
			fileInputStreamReader.read(bytes);
			encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
			modelAndView.addObject("encodeImage", encodedfile);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

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

	@RequestMapping(value="/error", method = RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("error");
		return modelAndView;
	}

}
