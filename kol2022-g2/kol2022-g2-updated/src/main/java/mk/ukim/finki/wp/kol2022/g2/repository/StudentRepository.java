package mk.ukim.finki.wp.kol2022.g2.repository;

import mk.ukim.finki.wp.kol2022.g2.model.Course;
import mk.ukim.finki.wp.kol2022.g2.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {

    List<Student> findAllByCoursesAndEnrollmentDate(Course course, LocalDate enrollmentDate);
    List<Student> findAllByCourses(Course course);
    List<Student> findAllByEnrollmentDate(LocalDate enrollmentDate);

    Optional<Student> findByName(String username);
}
