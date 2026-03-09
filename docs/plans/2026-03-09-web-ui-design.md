# Web UI Design - Admin Dashboard

## Overview
Add a Scala.js + Laminar web UI to the existing ZIO backend for full CRUD management of Departments, Employees, and Phones with search and detail views.

## Architecture

### Project Structure
```
zio-backend-example/
├── ui/                     # NEW: Scala.js + Laminar project
│   ├── src/main/scala/     # Laminar components
│   └── build.sbt
├── app/                    # Modified: serves static assets
├── core/
├── domain/
└── endpoints/
```

### Communication
- Browser → REST API (fetch) → ZIO HTTP backend
- Domain types shared via `domain` JAR dependency

### Backend Changes
- Serve static files from `/ui/*` in Router
- Enable CORS for development

## UI Structure

### Pages
1. **Dashboard** - Overview/summary
2. **Departments** - List with search, CRUD, detail view
3. **Employees** - List with search, CRUD, detail view (shows phones)
4. **Phones** - List with search, CRUD

### Components
- Navigation sidebar
- DataTable with sorting/filtering
- Modal forms for create/edit
- Detail panels

## API Endpoints Used
- Department: create, getAll, getById, update, delete
- Employee: create, getAll, getById, update, delete
- Phone: create, getById, update, delete
- EmployeePhone: addPhone, getPhones, removePhone

## Build Setup
- Scala.js 1.x
- Laminar for UI
- Vite or scalajs-bundler for bundling
- CORS enabled on backend for dev
