# Copilot instructions for the finite elements simulation project

## Build, test, and validation

This is a Java 17 Maven project with a Swing desktop UI and a Barnes-Hut style gravity simulation.

- Use `mvn test` for the default full test suite.
- Run a single JUnit class with `mvn -Dtest=MassBodyTreeImplTest test`.
- Run one test method with `mvn -Dtest=MassBodyTreeImplTest#simplePointsSequenceTest test`.
- Run the app locally with `mvn spring-boot:run`.
- Build a jar with `mvn package`.
- The repository includes a `mvnw` script, but this checkout does not have the Maven wrapper files (`.mvn/wrapper/...`), so use `mvn` directly rather than `./mvnw`.
- There is no dedicated lint task configured in `pom.xml`; Maven compilation and the test suite are the repository's main validation checks.

## High-level architecture

The code is organized around a gravity simulation loop that updates a quadtree of massive bodies and draws the result in a Swing canvas.

- `src/main/java/com/psz/graphics/finelmsim/FiniteElementsSimulationApplication.java`
  - Bootstraps the app as a Spring Boot application with `headless(false)` and `WebApplicationType.NONE`.
  - The desktop UI is launched from `MainFrame` through `ApplicationRunner`.
- `src/main/java/com/psz/graphics/finelmsim/ui/`
  - `MainFrame` wires up the Swing UI, generator selection, and simulation start/stop controls.
  - `MassTreeCanvas` is the rendering layer; it draws the quadtree, bodies, and simulation overlays.
- `src/main/java/com/psz/graphics/finelmsim/domain/generator/`
  - `MassBodyGenerator` defines the contract for creating initial mass distributions.
  - Concrete generators such as `RandomBodiesGenerator` produce the initial body lists used by the simulation.
- `src/main/java/com/psz/graphics/finelmsim/domain/simulator/`
  - `GravitySimulator` owns the runtime loop, frame timing, and repaint cadence.
  - `GravitySimulator2DCalculation` performs the per-step update: mass distribution, force calculation, body movement, and tree repair.
- `src/main/java/com/psz/graphics/finelmsim/domain/tree/`
  - `MassBodyTreeImpl` is the main quadtree implementation.
  - `TreeNode` represents each node with a center, size, optional body payload, and aggregated center-of-mass data.
  - The tree is used for Barnes-Hut approximation: `calculateMassDistribution()` builds aggregate mass and `getAttractors()` extracts far-away cluster representatives instead of evaluating every pair of bodies.
- `src/main/java/com/psz/graphics/finelmsim/domain/element/`
  - `MassiveBody` stores mass, position, velocity, and acceleration.
  - `Position` is the geometric value object used throughout the simulation and tree logic.

The project is not a layered web application; it is a computational visualization app where the simulation domain and UI are tightly coupled.

## Key conventions

- This repository uses Lombok heavily (`@Slf4j`, `@Data`, etc.), so code often relies on generated getters/setters/toString methods.
- Tests are JUnit 5 and many existing tests are structural: they assert exact node counts and mass values in the quadtree rather than only checking end-to-end output.
- The simulation code mutates `Position` and `MassiveBody` instances in place during each step to avoid creating large numbers of short-lived objects; when changing force or movement logic, keep that in-place pattern in mind.
- Tree operations are performance-sensitive and use manual index-based storage rather than a generic collection structure. Be careful when changing `MassBodyTreeImpl` node indexing, child offsets, or `TreeNode` splitting logic.
- The simulation uses a coarse-grain Barnes-Hut approximation with a `theta` parameter. When updating attractor logic, preserve the relationship between distance, mass, and the tree-splitting condition.
- `GravitySimulator` and `MassTreeCanvas` use `Optional` and direct UI redraws, so view updates are intentionally coupled to simulation state. Keep visual and simulation state changes synchronized when editing the painting pipeline.
- The app is configured as a Spring Boot app, but it runs without a web server; the main runtime is a desktop window plus background simulation thread.

## Repository-specific notes

- Keep package names and package structure under `com.psz.graphics.finelmsim.*` consistent when adding new classes.
- Prefer small, behavior-focused tests that align with the existing tree-structure assertions in `MassBodyTreeImplTest`.
- If you add a new generator, implement `MassBodyGenerator` and register it as a Spring `@Component` so it appears in the UI selector automatically.
- Favor direct mutation of the simulation model state over introducing a broader abstraction unless the change clearly spans multiple simulation stages.
