package com.example.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepository;
	@Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public List<User> findAllUser(){
		return userRepository.findAll();
	}

	public User findUserById(Integer id){
		return userRepository.findById(id);
	}

	@Override
	public void saveUser(User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(1);
        Role userRole = roleRepository.findByRole("USER");
        user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
		userRepository.save(user);
	}

	@Override
	public void softDeleteUser(User user){
		user.setActive(0);
		userRepository.save(user);
	}

	public void updateUser(User user){
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setActive(user.getActive());
		user.setEmail(user.getEmail());
		user.setLastName(user.getLastName());
		user.setRoles(user.getRoles());
		userRepository.save(user);
	}

	public List<Role> finAllRoles(){
		return roleRepository.findAll();
	}

}
