package com.csye6225.Assignment3.services;

import com.csye6225.Assignment3.repository.AssignmentRepository;
import com.csye6225.Assignment3.auth.BasicAuthenticationManager;
import com.csye6225.Assignment3.entity.Assignment;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;

    @Autowired
    private final BasicAuthenticationManager authenticationManager;
    @Autowired
    private EntityManager entityManager;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, BasicAuthenticationManager authenticationManager) {
        this.assignmentRepository = assignmentRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void createAssignment(JsonNode reqNode) {
        Assignment assignment = new Assignment();
        assignment.setName(reqNode.get("name").textValue());
        assignment.setPoints(reqNode.get("points").intValue());
        assignment.setNumber_of_Attempts(reqNode.get("number_of_attempts").intValue());
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        LocalDateTime date = LocalDateTime.parse(reqNode.get("deadline").textValue(), inputFormatter);
        assignment.setDeadline(date);
        assignment.setAssignmentCreated(LocalDateTime.now());
        assignment.setAssignmentUpdated(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assignment.setOwnerEmail(authentication.getName());
        assignmentRepository.save(assignment);
    }


    @Override
    public Assignment getOneAssignment(String id) {
        return assignmentRepository.findById(id);
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    @Transactional
    public boolean deleteAssignment(String id) {

        Assignment assignment = assignmentRepository.findById(id);
        if (assignment == null)
            return false;
        if (assignment.getOwnerEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            Query query = entityManager.createQuery("delete from Assignment a WHERE a.id=:id");
            query.setParameter("id", id);
            query.executeUpdate();
            return true;
        }
        return false;
    }
    @Override
    public boolean updateAssignment(String id, Assignment requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assignment assignment1 = assignmentRepository
                .findById(id);
        if (assignment1 == null){
            return false;
        }
        if (authentication.getPrincipal().equals(assignment1.getOwnerEmail())){
            assignment1.setName(requestBody.getName());
            assignment1.setPoints(requestBody.getPoints());
            assignment1.setNumber_of_Attempts(requestBody.getNumber_of_Attempts());
            return assignmentRepository.save(assignment1) != null ? true : false;
        }
        else
            return false;
    }


}




