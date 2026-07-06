package com.financetracker.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financetracker.model.Tag;
import com.financetracker.model.User;

public interface TagRepository extends JpaRepository<Tag, String> {

    List<Tag> findByUserOrderByNameAsc(User user);

    Optional<Tag> findByIdAndUser(String id, User user);

    boolean existsByNameIgnoreCaseAndUser(String name, User user);

    List<Tag> findAllByIdInAndUser(Collection<String> ids, User user);

}
