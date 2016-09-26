package com.lesfurets.maven.partial.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.*;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class DependencyUtilsTest {

    private static final String VERSION = "1.0";
    private static final String GROUP_ID = "com.test";

    private MavenProject parent;
    private MavenProject m1;
    private MavenProject m2;
    private MavenProject m3;
    private MavenProject m4;
    private MavenProject m5;
    private MavenProject m6;
    private MavenProject m7;

    private List<MavenProject> allProjects;

    @Before
    public void setUp() {
        parent = new MavenProject();
        m1 = new MavenProject();
        m2 = new MavenProject();
        m3 = new MavenProject();
        m4 = new MavenProject();
        m5 = new MavenProject();
        m6 = new MavenProject();
        m7 = new MavenProject();

        parent.setArtifactId("parent");
        parent.setGroupId(GROUP_ID);
        parent.setVersion(VERSION);

        m1.setParent(parent);
        m1.setArtifactId("m1");
        m1.setGroupId(GROUP_ID);
        m1.setVersion(VERSION);
        Dependency d1 = new Dependency();
        d1.setArtifactId("m1");
        d1.setGroupId(GROUP_ID);
        d1.setVersion(VERSION);

        m2.setParent(parent);
        m2.setGroupId(GROUP_ID);
        m2.setArtifactId("m2");
        m2.setVersion(VERSION);
        Dependency d2 = new Dependency();
        d2.setArtifactId("m2");
        d2.setGroupId(GROUP_ID);
        d2.setVersion(VERSION);

        m3.setParent(parent);
        m3.setGroupId(GROUP_ID);
        m3.setArtifactId("m3");
        m3.setVersion(VERSION);
        Dependency d3 = new Dependency();
        d3.setArtifactId("m3");
        d3.setGroupId(GROUP_ID);
        d3.setVersion(VERSION);

        m4.setGroupId(GROUP_ID);
        m4.setArtifactId("m4");
        m4.setVersion(VERSION);
        Dependency d4 = new Dependency();
        d4.setArtifactId("m4");
        d4.setGroupId(GROUP_ID);
        d4.setVersion(VERSION);

        m5.setGroupId(GROUP_ID);
        m5.setArtifactId("m5");
        m5.setVersion(VERSION);
        Dependency d5 = new Dependency();
        d5.setArtifactId("m5");
        d5.setGroupId(GROUP_ID);
        d5.setVersion(VERSION);

        m6.setParent(parent);
        m6.setGroupId(GROUP_ID);
        m6.setArtifactId("m6");
        m6.setVersion(VERSION);
        Dependency d6 = new Dependency();
        d6.setArtifactId("m6");
        d6.setGroupId(GROUP_ID);
        d6.setVersion(VERSION);

        m7.setParent(parent);
        m7.setGroupId(GROUP_ID);
        m7.setArtifactId("m7");
        m7.setVersion(VERSION);

        Dependency d7 = new Dependency();
        d7.setArtifactId("m7");
        d7.setGroupId(GROUP_ID);
        d7.setVersion(VERSION);

        //        m5 --> m2
        m5.setDependencies(Arrays.asList(d2));
        //        m3 --> m2
        //        p <!-- m3
        m3.setDependencies(Arrays.asList(d2));
        //        m1 --> m4
        //        m1 --> m3
        //        p <!-- m1
        m1.setDependencies(Arrays.asList(d4, d3));
        //        m6 --> m7
        //        p <!-- m6
        m6.setDependencies(Arrays.asList(d7));
        //        p --> d1
        parent.setDependencies(Arrays.asList(d1));

        Build build = new Build();
        Plugin plugin = new Plugin();
        plugin.setArtifactId(m7.getArtifactId());
        plugin.setGroupId(m7.getGroupId());
        plugin.setVersion(m7.getVersion());
        build.addPlugin(plugin);
        m5.setBuild(build);

        allProjects = Arrays.asList(parent, m1, m2, m3, m4, m5, m6, m7);
    }

    @Test
    public void collectAllDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m2, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m5, m6, m7).collect(Collectors.toSet()));
    }

    @Test
    public void collectNoDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m5, dependents);
        assertThat(dependents).isEqualTo(Collections.emptySet());
    }

    @Test
    public void collectDependentsTransitiveM1() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m1, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m6, m7).collect(Collectors.toSet()));
    }

    @Test
    public void collectDependentsTransitiveM3() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m3, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m6, m7).collect(Collectors.toSet()));
    }

    @Test
    public void collectTransitiveDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m4, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m6, m7).collect(Collectors.toSet()));
    }

    @Test
    public void collectTransitiveDependentsParent() {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, parent, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m1, m2, m3, m6, m7).collect(Collectors.toSet()));
    }

    @Test
    public void collectTransitiveDependentsDontFollowParent() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m7, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m6, m5).collect(Collectors.toSet()));
    }

    @Test
    public void getAllDependencies() throws Exception {
        Set<MavenProject> dependencies = DependencyUtils.getAllDependencies(allProjects, m1);
        assertThat(dependencies).isEqualTo(Stream.of(m1, m3, m4, m2).collect(Collectors.toSet()));
    }

    @Test
    public void collectNoDependencies() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m5, dependents);
        assertThat(dependents).isEqualTo(Collections.emptySet());
    }

    @Test
    public void collectParentDependencies() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(Arrays.asList(m4, m1, parent), m4, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m1, parent).collect(Collectors.toSet()));
    }
}