package mk.ukim.finki.wp.kol2022.g2.service.impl;


import mk.ukim.finki.wp.kol2022.g2.model.Course;
import mk.ukim.finki.wp.kol2022.g2.model.Student;
import mk.ukim.finki.wp.kol2022.g2.model.StudentType;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidCourseIdException;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidNameException;
import mk.ukim.finki.wp.kol2022.g2.repository.CourseRepository;
import mk.ukim.finki.wp.kol2022.g2.repository.StudentRepository;
import mk.ukim.finki.wp.kol2022.g2.service.StudentService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class StudentServiceImpl implements StudentService , UserDetailsService {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private PasswordEncoder passwordEncoder;

    public StudentServiceImpl(CourseRepository courseRepository, StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder =passwordEncoder;
    }


    @Override
    public List<Student> listAll() {
        return this.studentRepository.findAll();
    }

    @Override
    public Student findById(Long id) {
        return this.studentRepository.findById(id).orElseThrow(InvalidCourseIdException::new);
    }

    @Override
    public Student create(String name, String email, String password, StudentType type, List<Long> courseId, LocalDate enrollmentDate) {
        String encryptedPassword = this.passwordEncoder.encode(password);
        List<Course> courses = this.courseRepository.findAllById(courseId);
        Student student  = new Student(name,email,encryptedPassword,type, courses, enrollmentDate);

        return this.studentRepository.save(student);
    }

    @Override
    public Student update(Long id, String name, String email, String password, StudentType type, List<Long> coursesId, LocalDate enrollmentDate) {
        //dali tuka treba da bide enkriptiran pasvordo
        String encryptedPassword = this.passwordEncoder.encode(password);
        Student student = this.findById(id);
        student.setName(name);
        student.setEmail(email);
        student.setPassword(encryptedPassword);
        student.setType(type);
        student.setEnrollmentDate(enrollmentDate);

        List<Course> courses=this.courseRepository.findAllById(coursesId);
        student.setCourses(courses);

        return this.studentRepository.save(student);
    }

    @Override
    public Student delete(Long id) {
        Student student= this.findById(id);
        this.studentRepository.delete(student);
        return student;
    }

    @Override
    public List<Student> filter(Long courseId, Integer yearsOfStudying) {

        Course course= courseId != null ? this.courseRepository.findById(courseId).orElse(null): null;

        if(courseId != null && yearsOfStudying != null) {
            LocalDate now= java.time.LocalDate.now();
            LocalDate enrollmentDate= now.minusYears(yearsOfStudying);
            return this.studentRepository.findAllByCoursesAndEnrollmentDate(course, enrollmentDate);

        }else if(courseId != null) {
            return this.studentRepository.findAllByCourses(course);

        }else if (yearsOfStudying != null) {
            LocalDate now = java.time.LocalDate.now();
            LocalDate enrollmentDate = now.minusYears(yearsOfStudying);
            return this.studentRepository.findAllByEnrollmentDate(enrollmentDate);
        }else return this.studentRepository.findAll();


    }

    @Override
    public Student create(String name, String password, StudentType type) {
        String encryptedPassword = this.passwordEncoder.encode(password);

        Student student  = new Student(name,encryptedPassword,type);

        return this.studentRepository.save(student);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Student student= this.studentRepository.findByName(username).orElseThrow(InvalidNameException::new);

        UserDetails userDetails= new org.springframework.security.core.userdetails.User(student.getName(), student.getPassword(), Stream.of(new SimpleGrantedAuthority(student.getType().toString())).collect(Collectors.toList()));

        return userDetails;
    }
}
