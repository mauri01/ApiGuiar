package com.example.service;

import com.example.model.Role;
import com.example.model.User;

import java.util.List;

public interface UserService {
	public User findUserByEmail(String email);
	public User findUserById(Integer id);
	public List<User> findAllUser();
	public void saveUser(User user);
	public void softDeleteUser(User user);
	public void updateUser(User user);
	public List<Role> finAllRoles();
}
