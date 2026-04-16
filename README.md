# 🖥️ CPU Scheduler Simulator

> A desktop GUI application that visualizes and simulates classic CPU scheduling algorithms — built for **CSE335s (UG2023) - Operating Systems**.

---

## 📋 Overview

The CPU Scheduler Simulator is an interactive desktop application that lets users define processes, select a scheduling algorithm, and observe how the CPU handles process execution in real time.

In a modern operating system, multiple processes compete for a single CPU. Since only one process can execute at a time, the OS must decide **which process runs next and for how long** — this is the job of a CPU scheduler. A well-designed scheduling algorithm maximizes CPU utilization, minimizes waiting time, and ensures fairness among all running processes.

This project implements and simulates four classic CPU scheduling algorithms with full visual feedback:

- A dynamic **Gantt Chart** that builds in real time
- A live-updating **process table** showing remaining burst time per process
- Calculated **average waiting time** and **average turnaround time** after each run

---

## ⚙️ Scheduling Algorithms

### 1. First-Come, First-Served (FCFS)

The simplest scheduling algorithm — processes are executed in the exact order they arrive, like a queue at a counter.

- **Type:** Non-preemptive
- **Strategy:** FIFO — first arrived, first served
- **Pros:** Simple, fair in arrival order, no starvation
- **Cons:** Convoy effect — one long process can delay all shorter ones behind it, leading to high average waiting time
- **Best for:** Batch systems where simplicity matters more than response time

**Example (from lecture slides):**
```
Process   Burst Time
P1        24
P2         3
P3         3

Order: P1 → P2 → P3
Avg Waiting Time = (0 + 24 + 27) / 3 = 17 ms
```

---

### 2. Shortest Job First (SJF)

Schedules the process with the shortest burst time next, minimizing the average waiting time across all processes.

#### 2.1 Preemptive (Shortest Remaining Time First — SRTF)
- If a new process arrives with a shorter **remaining** burst time than the currently running process, the CPU is preempted immediately
- Theoretically optimal for minimizing average waiting time
- High context-switching overhead
- Risk of **starvation** for long processes if short ones keep arriving

#### 2.2 Non-Preemptive
- At each scheduling point (when the CPU becomes free), the process with the shortest burst time in the ready queue is selected
- Once a process starts executing, it runs to completion — no interruption
- Minimizes average waiting time among non-preemptive algorithms
- Same starvation risk for long processes

**Example (Preemptive, from lecture slides):**
```
Process   Arrival   Burst
P1           0        8
P2           1        4
P3           2        9
P4           3        5

Gantt: P1 | P2 | P4 | P1 | P3
       0    1    5   10   17   26

Avg Waiting Time = [(10-1) + (1-1) + (17-2) + (5-3)] / 4 = 6.5 ms
```

---

### 3. Priority Scheduling

Each process is assigned a numeric priority. The CPU always executes the highest-priority process available. In this simulator, **lower number = higher priority**.

- Available in both **preemptive** and **non-preemptive** modes
- Preemptive variant: a higher-priority arrival immediately preempts the running process
- Non-preemptive variant: priority is only checked when the CPU becomes free
- **Starvation risk:** low-priority processes may never get CPU time if high-priority ones keep arriving
- **Aging** (gradually increasing priority of waiting processes) is a standard remedy for starvation
- Suitable for **real-time systems** where urgent tasks must be guaranteed CPU access

---

### 4. Round Robin (RR)

Each process receives a fixed slice of CPU time called the **time quantum**. After the quantum expires, the process is preempted and moved to the back of the ready queue, giving the next process its turn.

- **Type:** Preemptive
- **Fairness:** Every process gets CPU time — no starvation
- If there are `n` processes and quantum is `q`, no process waits more than `(n-1) × q` time units
- **Quantum size matters:**
  - Too large → behaves like FCFS
  - Too small → excessive context switching overhead
  - Ideal: large relative to context-switch time, small enough for good responsiveness
- Best suited for **time-sharing and interactive systems**

**Example (from lecture slides, quantum = 4):**
```
Process   Burst Time
P1        24
P2         3
P3         3

Gantt: P1 | P2 | P3 | P1 | P1 | P1 | P1 | P1
       0    4    7   10   14   18   22   26   30

Avg Waiting Time = 5.67 ms
```

---

## 🖱️ Operating Modes

### Mode 1 — Static Mode

All processes are entered before the simulation begins (all with arrival time = 0 or custom values). The scheduler computes the complete schedule in a single pass and renders the final Gantt Chart immediately.

- No interaction during execution
- No processes can be added or modified mid-run
- Time is not animated — results appear instantly
- Best for quick analysis, homework verification, and algorithm comparison

### Mode 2 — Live Simulation Mode

A real-time system clock ticks continuously — each unit of time equals one second. The Gantt Chart grows dynamically as the simulation progresses.

- **Pause / Resume** button gives full control over execution flow
- While paused, new processes can be **injected dynamically** into the scheduler
- The arrival time of any newly added process is automatically set to the current clock time at the moment of pause
- After resuming, the scheduler seamlessly incorporates the new process and continues
- Closely mimics the behavior of a real operating system handling concurrent, dynamically-arriving processes

---

## 📊 Performance Metrics

| Metric | Formula | Description |
|---|---|---|
| **Burst Time** | — | Total CPU time a process needs to complete |
| **Waiting Time** | Turnaround − Burst | Time spent waiting in the ready queue |
| **Turnaround Time** | Completion − Arrival | Total time from submission to completion |
| **Avg Waiting Time** | Σ Waiting / n | Mean waiting time across all processes |
| **Avg Turnaround Time** | Σ Turnaround / n | Mean turnaround time across all processes |

---

## 🧪 Test Cases & Validation

Every algorithm was tested against examples taken directly from lecture slides and problem sheets to verify correctness:

| Algorithm | Mode | Test Cases | Verified Against |
|---|---|---|---|
| FCFS | Static | EX_1, EX_2, EX_3 | Lecture slides + Sheet 5 |
| SJF Preemptive | Static + Dynamic | EX_1, EX_2 | Lecture slides |
| SJF Non-Preemptive | Static | EX_1, EX_2 | Lecture slides + Sheet 5 |
| Priority Preemptive | Static + Dynamic | EX_1, EX_2 | Lecture slides |
| Priority Non-Preemptive | Static | EX_1 | Sheet 5 |
| Round Robin | Static + Dynamic | EX_1, EX_2, EX_3 | Lecture slides + Sheet 5 |

All simulation outputs — Gantt Charts, average waiting times, and average turnaround times — matched the expected values from the reference materials.

---

## 🔁 Algorithm Comparison

| Algorithm | Preemptive | Starvation | Overhead | Best Use Case |
|---|---|---|---|---|
| FCFS | No | No | Low | Batch processing |
| SJF Non-Preemptive | No | Yes | Low | Known burst times |
| SJF Preemptive (SRTF) | Yes | Yes | High | Minimizing wait time |
| Priority Non-Preemptive | No | Yes | Low | Priority-based batch |
| Priority Preemptive | Yes | Yes | High | Real-time systems |
| Round Robin | Yes | No | Medium | Time-sharing / interactive |

---

## 📥 Download

| Resource | Link |
|---|---|
| 📦 Executable (.exe) | [Download](https://drive.google.com/file/d/1XDMg1b9wHc6ktiGGLE2s7Y6ljRu7T7tQ/view?usp=drive_link) |
| 🎬 SJF Preemptive + Priority Dynamic Demo | [Watch](https://drive.google.com/file/d/1R0Z8PPJqr6ZteCVBq5nJZbf3P7KHk4cd/view?usp=drive_link) |
| 🎬 SJF Non-Preemptive + RR Dynamic Demo | [Watch](https://drive.google.com/file/d/1lhc1eVW415UppIPMzOEIb1-KpG5XN9oY/view?usp=drive_link) |
| 📁 Full Google Drive Folder | [Open](https://drive.google.com/drive/folders/1l0yn5xbzmHOUyp6gX9xIPCoMst41UUgJ?usp=drive_link) |
