package com.fererlab.example.user.api;

import java.util.List;

public interface UserService {

    List<String> getAllUsers();

    List<String> getAllUsersOfGroup(String groupname);

}
