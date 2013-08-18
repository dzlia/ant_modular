Ant Modular
===========

Ant tasks that allow you to build multi-module applications with ease.

Introduction
------------

Though it is a powerful imperative build toolkit, Ant lacks support of multi-module applications.
It is common to see that such applications are built with Ant using home-grown (often primitive) tools.
In particular, dependencies between modules are managed manually directly in the build script
by means of &lt;ant&gt; and &lt;antcall&gt; glue; there is often duplication in module-specific build scripts,
if they are used. The cost to add another module or another library to a module is quite big and
this task is error-prone.

If you see your build environment described, then Ant Modular could solve these problems, still
allowing for using all power and implementation freedom Ant provides.

Ant Modular introduces a term "module". A "module" here is a directory with known structure and
content with meta information attached to it. Meta information includes the dependencies
between modules as well as other arbitrary attributes. Ant Modular uses these dependencies
to determine in which order the modules are to be built. In addition, if there are module attributes
that need to be propagated from the dependee modules to the depender module (e.g. the module classpath).
Other meta information is available in the build context associated with a given module.

### Basics
The core of Ant Modular is the `<callTargetForModules>` Ant task. It scans though the modules that
are directly specified and all their direct and indirect dependee modules, defines a global order
of execution and call a given target for each module so that each module is processed after
all its direct dependee modules have been processed. Each target is invoked in its own project,
so that the modules are processed in an isolated context. The `Module` descriptor is passed
as a property to each target and is available to the helper Ant tasks.

The target invoked is the same for each module and is defined in the same ("master") build script.
This effectively eliminates duplication between uniform modules, because the code which is the same
(or very similar) for each module is defined in a single script.

Module-specific information such as module directory or module attributes is obtained from the module
descriptor.

The `<callTargetForModules>` task is configured with a `ModuleLoader` component. Any Ant type
that implements the interface `afc.ant.modular.ModuleLoader` could be plugged in. This allows the
build engineer to choose a meta information format that fits the requirements best. It could be
a Jar Manifest file or some form of XML or a properties file.

### Helper Ant tasks
The following Ant tasks are used within the target invoked by `<callTargetForModules>` to obtain
module-specific meta information. Note that other tasks could be created and used freely.

* `<getModulePath>` - sets the module path to a given property.
* `<getModuleAttribute>` - sets the module attribute with a given name to a given property.
* `<getModuleClasspath>` - resolves the module classpath into an Ant `Path` object and set it to
		a given property. If needed, the classpaths of the dependee modules could be merged into
		the result classpath (enabled by default).

### Module structure and meta information
In terms of Ant Modular a module is a directory that is known to follow some conventions.
Each module is defined purely by its path relative to the Ant project base directory.

Ant Modular does not impose any requirements to the module structure and content. It is purely
defined by the build engineer. However, it is easier to manage modules that have uniform structure.

Module meta information can have any format, given that there is a `ModuleLoader` component that
is capable of loading this information into the Ant Modular object model.
Meta information should provide information about modules this module directly depends upon.
Other attributes are of free choice.
