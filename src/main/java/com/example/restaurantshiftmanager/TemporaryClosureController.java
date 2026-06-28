package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class TemporaryClosureController {

    private final TemporaryClosureRepository temporaryClosureRepository;

    public TemporaryClosureController(TemporaryClosureRepository temporaryClosureRepository) {
        this.temporaryClosureRepository = temporaryClosureRepository;
    }

    @GetMapping("/temporary-closures")
    public String list(Model model) {
        List<TemporaryClosure> temporaryClosures = temporaryClosureRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(TemporaryClosure::getClosureDate))
                .toList();

        model.addAttribute("temporaryClosures", temporaryClosures);

        return "temporary-closures/list";
    }

    @GetMapping("/temporary-closures/new")
    public String newForm() {
        return "temporary-closures/form";
    }

    @PostMapping("/temporary-closures")
    public String create(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closureDate,
            @RequestParam(required = false) String reason,
            Model model
    ) {
        String errorMessage = validateTemporaryClosure(closureDate);

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("closureDate", closureDate);
            model.addAttribute("reason", reason);

            return "temporary-closures/form";
        }

        TemporaryClosure temporaryClosure = new TemporaryClosure(closureDate, reason);
        temporaryClosureRepository.save(temporaryClosure);

        return "redirect:/temporary-closures";
    }

    @PostMapping("/temporary-closures/{id}/delete")
    public String delete(@PathVariable Long id) {
        temporaryClosureRepository.deleteById(id);

        return "redirect:/temporary-closures";
    }

    private String validateTemporaryClosure(LocalDate closureDate) {
        if (closureDate == null) {
            return "日付を入力してください。";
        }

        List<TemporaryClosure> temporaryClosures = temporaryClosureRepository.findAll();

        for (TemporaryClosure temporaryClosure : temporaryClosures) {
            if (temporaryClosure.getClosureDate().equals(closureDate)) {
                return "同じ日付の臨時休業が既に登録されています。";
            }
        }

        return null;
    }
}
