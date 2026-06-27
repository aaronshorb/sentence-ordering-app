document.addEventListener("DOMContentLoaded", () => {
    const list = document.querySelector("[data-sortable-sentences]");

    if (!list || typeof Sortable === "undefined") {
        setupAudioPlayers();
        return;
    }

    storeFixedSentenceSlots(list);
    updateSentenceNumbers(list);

    // Keeps correct rows in place while the user reorders the rest.
    const hasFixedSentenceSlots = list.querySelector(".sentence-row.is-fixed") !== null;
    let fixedSlotRestoreFrame = null;
    const refreshSentenceOrder = () => {
        if (hasFixedSentenceSlots) {
            restoreFixedSentenceSlots(list);
            clearFixedSentenceMotion(list);
        }

        updateSentenceNumbers(list);
    };
    const keepFixedSentenceSlots = () => {
        if (fixedSlotRestoreFrame !== null) {
            return;
        }

        fixedSlotRestoreFrame = window.requestAnimationFrame(() => {
            fixedSlotRestoreFrame = null;
            refreshSentenceOrder();
        });
    };
    const restoreFixedSentenceSlotsNow = () => {
        if (fixedSlotRestoreFrame !== null) {
            window.cancelAnimationFrame(fixedSlotRestoreFrame);
            fixedSlotRestoreFrame = null;
        }

        refreshSentenceOrder();
    };

    Sortable.create(list, {
        animation: hasFixedSentenceSlots ? 0 : 150,
        draggable: ".sentence-row:not(.is-fixed)",
        filter: ".sentence-row.is-fixed",
        preventOnFilter: false,
        ghostClass: "sentence-row-ghost",
        chosenClass: "sentence-row-chosen",
        dragClass: "sentence-row-drag",
        fallbackClass: "sentence-row-fallback",
        forceFallback: true,
        fallbackOnBody: true,
        fallbackTolerance: 4,
        onStart: (event) => {
            lockSentenceRowSize(event.item);
        },
        onMove: (event) => {
            if (isFixedSentenceRow(event.related)) {
                keepFixedSentenceSlots();
                return false;
            }

            keepFixedSentenceSlots();
            return true;
        },
        onChange: keepFixedSentenceSlots,
        onSort: keepFixedSentenceSlots,
        onEnd: (event) => {
            unlockSentenceRowSize(event.item);
            restoreFixedSentenceSlotsNow();
        }
    });

    setupAudioPlayers();
});

// Updates the visible row numbers after sorting.
function updateSentenceNumbers(list) {
    list.querySelectorAll(".sentence-row").forEach((row, index) => {
        const number = row.querySelector(".sentence-number");

        if (number) {
            number.textContent = String(index + 1);
        }
    });
}

function isFixedSentenceRow(row) {
    return row && row.classList.contains("sentence-row") && row.classList.contains("is-fixed");
}

function lockSentenceRowSize(row) {
    if (!row) {
        return;
    }

    const bounds = row.getBoundingClientRect();

    if (bounds.width <= 0 || bounds.height <= 0) {
        return;
    }

    row.style.width = `${bounds.width}px`;
    row.style.height = `${bounds.height}px`;
}

function unlockSentenceRowSize(row) {
    if (!row) {
        return;
    }

    row.style.width = "";
    row.style.height = "";
}

function storeFixedSentenceSlots(list) {
    list.querySelectorAll(".sentence-row").forEach((row, index) => {
        if (row.classList.contains("is-fixed")) {
            row.dataset.fixedIndex = String(index);
        }
    });
}

// Moves fixed rows back to their saved positions.
function restoreFixedSentenceSlots(list) {
    const rows = Array.from(list.querySelectorAll(".sentence-row"));
    const fixedRows = rows
        .filter((row) => row.classList.contains("is-fixed"))
        .map((row) => ({
            row,
            index: Number(row.dataset.fixedIndex)
        }))
        .filter(({ index }) => Number.isInteger(index) && index >= 0 && index < rows.length)
        .sort((left, right) => left.index - right.index);

    if (fixedRows.length === 0) {
        return;
    }

    const movableRows = rows.filter((row) => !row.classList.contains("is-fixed"));
    const orderedRows = new Array(rows.length);

    fixedRows.forEach(({ row, index }) => {
        orderedRows[index] = row;
    });

    let movableIndex = 0;

    for (let index = 0; index < orderedRows.length; index += 1) {
        if (!orderedRows[index]) {
            orderedRows[index] = movableRows[movableIndex];
            movableIndex += 1;
        }
    }

    if (rows.every((row, index) => row === orderedRows[index])) {
        return;
    }

    orderedRows.forEach((row) => list.appendChild(row));
}

function clearFixedSentenceMotion(list) {
    list.querySelectorAll(".sentence-row.is-fixed").forEach((row) => {
        row.style.transform = "";
        row.style.transition = "";
    });
}

// Connects the custom controls to the audio player.
function setupAudioPlayers() {
    document.querySelectorAll(".audio-strip").forEach((player) => {
        const audio = player.querySelector("[data-audio-player]");
        const toggle = player.querySelector("[data-audio-toggle]");
        const progress = player.querySelector("[data-audio-progress]");
        const volumeToggle = player.querySelector("[data-audio-volume-toggle]");

        if (!audio || !toggle || !progress || !volumeToggle) {
            return;
        }

        const updateProgress = () => {
            const percent = audio.duration
                ? (audio.currentTime / audio.duration) * 100
                : Number(progress.value);
            const safePercent = Number.isFinite(percent) ? percent : 0;

            progress.value = String(safePercent);
            progress.style.setProperty("--audio-progress", `${safePercent}%`);
        };

        const updateMutedState = () => {
            volumeToggle.classList.toggle("is-muted", audio.muted);
            volumeToggle.setAttribute("aria-label", audio.muted ? "Unmute audio" : "Mute audio");
        };

        const setPlaying = (isPlaying) => {
            toggle.classList.toggle("is-playing", isPlaying);
            toggle.setAttribute("aria-label", isPlaying ? "Pause audio" : "Play audio");
        };

        toggle.addEventListener("click", () => {
            if (audio.paused || audio.ended) {
                audio.play().catch(() => setPlaying(false));
                return;
            }

            audio.pause();
        });

        progress.addEventListener("input", () => {
            progress.style.setProperty("--audio-progress", `${progress.value}%`);

            if (audio.duration) {
                audio.currentTime = (Number(progress.value) / 100) * audio.duration;
            }
        });

        volumeToggle.addEventListener("click", () => {
            audio.muted = !audio.muted;
            updateMutedState();
        });

        audio.addEventListener("loadedmetadata", updateProgress);
        audio.addEventListener("timeupdate", updateProgress);
        audio.addEventListener("play", () => setPlaying(true));
        audio.addEventListener("pause", () => setPlaying(false));
        audio.addEventListener("ended", () => setPlaying(false));
        audio.addEventListener("error", () => {
            setPlaying(false);
            toggle.disabled = true;
            progress.disabled = true;
            volumeToggle.disabled = true;
        });

        updateProgress();
        updateMutedState();
    });
}
