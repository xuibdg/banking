package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.MUserRequest;
import com.core.banking.dto.MUserResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.MUserService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@NoArgsConstructor
public class MUserController {

    @Autowired
    private MUserService mUserService;


    @PostMapping
    String createUser(@RequestBody MUserRequest request,
                      @CurrentUser UserMetaData userMetaData) {
        return mUserService.createUser(request, userMetaData);
    }


    @GetMapping("/get-all")
    List<MUserResponse> getAll(@RequestParam(required = false, defaultValue = "") String name){
        return mUserService.getAll(name);
    }


    @PutMapping("/{id}")
    String updateUser(@PathVariable String id, @RequestBody MUserRequest request){
        return mUserService.updateUser(id, request);
    }


    @DeleteMapping("/{id}")
    String deletedUser(@PathVariable String id){
        return mUserService.deletedUser(id);
    }



}
