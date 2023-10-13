package br.com.joaocoqueiro.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaocoqueiro.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));
        var task = this.taskRepository.save(taskModel);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartedAt()) || currentDate.isAfter(taskModel.getEndedAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data precisa ser igual ou maior que a data atual");
        }

        if (taskModel.getStartedAt().isAfter(taskModel.getEndedAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início deve ser menor que a data de término");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(task);

    }

    @GetMapping("/")
    public ResponseEntity list(HttpServletRequest request) {
        var list = this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {

        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var updatedTask = this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

}
