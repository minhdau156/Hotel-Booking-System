// Shared quiz widget behaviour for the Swing teaching workspace.
// Markup contract per .quiz block:
//   <div class="quiz">
//     <p class="q">...question...</p>
//     <label><input type="radio" name="UNIQUE" data-correct="false"> ...option...</label>
//     <label><input type="radio" name="UNIQUE" data-correct="true"> ...option...</label>
//     <div class="feedback correct">...shown when a correct option is picked...</div>
//     <div class="feedback incorrect">...shown when a wrong option is picked...</div>
//   </div>
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".quiz").forEach((quiz) => {
    const inputs = quiz.querySelectorAll('input[type="radio"]');
    const correctBox = quiz.querySelector(".feedback.correct");
    const incorrectBox = quiz.querySelector(".feedback.incorrect");

    inputs.forEach((input) => {
      input.addEventListener("change", () => {
        const isCorrect = input.dataset.correct === "true";
        if (correctBox) correctBox.classList.toggle("shown", isCorrect);
        if (incorrectBox) incorrectBox.classList.toggle("shown", !isCorrect);
      });
    });
  });
});
