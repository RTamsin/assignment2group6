# Flappy Helicopter Game Plan

## Game Overview
The game is a 2D side-scrolling endless game built using `GameEngine.java`, inspired by Flappy Bird. Players control a helicopter in a town environment, avoiding building obstacles, collecting normal coins (often, 10 points each) for points and health coins (rarely, +1 extra life each). The game starts with 3 lives, losing 1 on collision, and records the top 3 scores. The difficulty increases as the game progresses (e.g., obstacle gaps narrow by 5% and building height increases by 10% every 20 points). It starts with an easy difficulty (moderate gaps, shorter buildings), becoming more challenging over time. It includes sprites, sound effects, a scoring system, and a lives system. If time allows, additional maps (e.g., forest) and objectives (e.g., firefighting, war battle) will be implemented. The plan is designed for a two-week timeline and split into four parts for a group of four.

## Development Plan: Division of Tasks

### Part 1: Core Gameplay and Helicopter Mechanics
**Assigned to: Group Member 1**

**Responsibilities:**
- Implement helicopter movement (flap on spacebar, gravity-based descent), collision detection, and lives system (starts with 3 lives, loses 1 on collision).
- Set initial difficulty (easy: moderate gaps, shorter buildings).
- Implement increasing difficulty (e.g., gaps narrow by 5% and building height increases by 10% every 20 points).
- Handle game states (start, playing, game over: lives = 0, restart).
- Implement scoring (10 points per normal coin collected).
- Add logic for health coins (rare, +1 life each).
- Record top 3 scores (store in a file or array).

**Deliverables:**
- Helicopter physics with flapping and gravity.
- Lives system (3 initial lives, health coins for extra lives).
- Dynamic difficulty adjustment based on score.
- Game state management (start, game over, restart).
- Scoring logic (normal coins only) and health coin logic.
- Top 3 score tracking.
- Test cases for collisions, scoring, coin collection, health coins, and difficulty scaling.

**Dependencies:**
- Obstacle and coin data from Part 2.
- Visual assets from Part 3.

### Part 2: Town Environment, Obstacles, and Coins
**Assigned to: Group Member 2**

**Responsibilities:**
- Design obstacle generation for town (building-shaped rectangles, starting with moderate gaps and shorter heights).
- Implement randomized obstacle spawning with dynamic difficulty adjustments (narrower gaps, taller buildings as score increases).
- Create a scrolling ground layer for movement effect.
- Add randomized coin spawning: normal coins (often, e.g., 70% spawn chance) and health coins (rarely, e.g., 5% spawn chance).
- Integrate obstacles and coins with collision detection.
- If time allows, prototype a forest map (tree obstacles) as a stretch goal.

**Deliverables:**
- Obstacle generation logic for town with dynamic difficulty.
- Scrolling ground rendering.
- Coin spawning logic (normal coins often, health coins rarely).
- Documentation on obstacle and coin properties for collision.
- Test cases for obstacle and coin spawning.
- Optional: Basic forest map prototype.

**Dependencies:**
- Visual assets from Part 3.
- Core mechanics from Part 1.

### Part 3: Visuals and Stretch Objectives
**Assigned to: Group Member 3**

**Responsibilities:**
- Source or create sprites for helicopter, building obstacles, town background (sky), normal coins (e.g., gold coin), and health coins (e.g., medkit or heart icon).
- Render town environment visuals with sprites or rectangles, adjusting building heights dynamically.
- Add basic animation (e.g., helicopter rotor spin via 2-3 sprite frames).
- If time allows, prototype stretch objectives:
  - Firefighting: Collect water drops (extra points) to simulate extinguishing fires.
  - War battle: Avoid enemy fire (additional moving obstacles).
- Ensure visuals align with stretch map (forest) if implemented.

**Deliverables:**
- Sprite images for helicopter, buildings, background, normal coins, and health coins.
- Visual rendering code for town with dynamic building heights.
- Rotor animation via sprite frames.
- Test cases for sprite rendering.
- Optional: Prototype firefighting or war battle objectives (e.g., collectible sprites, moving obstacles).

**Dependencies:**
- Obstacle and coin data from Part 2.
- Core mechanics from Part 1.

### Part 4: UI and Sound
**Assigned to: Group Member 4**

**Responsibilities:**
- Design main menu with an option to start the game.
- Implement in-game HUD showing score and lives.
- Create game over screen (lives = 0) with score, top 3 scores, and restart option.
- Source and integrate sound effects (flap, crash, coin collect, health coin collect) and background music.
- Implement top 3 score tracking (display and update).

**Deliverables:**
- Main menu for starting the game.
- HUD and game over screen with top 3 scores.
- Sound files (WAV) for flap, crash, coin collect, health coin collect, and music.
- Top 3 score system.
- Test cases for UI navigation and audio.

**Dependencies:**
- Core mechanics from Part 1.

## Timeline (2 Weeks)
- **Week 1**:
  - **Day 1-2**: Assign roles, set up project, source assets (helicopter sprite, flap sound).
  - **Day 3-4**: Implement core mechanics with dynamic difficulty (Part 1), town obstacles and coins (Part 2).
  - **Day 5-7**: Add town visuals and animation (Part 3).
- **Week 2**:
  - **Day 8-10**: Develop UI, integrate sounds (Part 4).
  - **Day 11-12**: Integrate parts, test core game (town, endless mode, dynamic difficulty, coin/health coin collection).
  - **Day 13-14**: Add stretch goals (forest map, firefighting/war objectives) if time allows; polish, prepare submission (code, 4-5 page document, 5-minute video).

## Tools and Resources
- **GameEngine.java**: For rendering, input, audio.
- **Sprites**: OpenGameArt.org for helicopter, buildings, sky, normal coins, health coins; optional forest trees, water drops, enemy fire.
- **Sounds**: Freesound.org for WAV files (flap, crash, coin collect, health coin collect, music).
- **Version Control**: GitHub.
- **IDE**: IntelliJ.

## Next Steps
- Assign members to Parts 1–4.
- Set up GitHub repository.
- Source one sprite (helicopter with rotor frames) and one sound (flap) by Day 2.
- Schedule mid-week check-in (Day 7) to assess progress and decide on stretch goals.