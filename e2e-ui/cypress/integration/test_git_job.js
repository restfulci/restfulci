describe('Hello-world Git Job Test', () => {
  it('Visit jobs index page', () => {
    cy.visit('http://localhost:3000/jobs')
    cy.contains('+').click()

    cy.url().should('include', '/jobs/add')
    cy.get('#name').type('cypress_git_job')
    cy.get('#remoteOrigin').type('https://github.com/restfulci/restfulci-examples.git')
    cy.get('#configFilepath').type('hello-world/restfulci.yml')
    cy.contains('Add git job').click()

    cy.url().should('match', /.*\/jobs\/\d+$/)
    cy.contains('cypress_git_job')

    cy.visit('http://localhost:3000/jobs')
    cy.contains('cypress_git_job').click()

    cy.url().should('match', /.*\/jobs\/\d+$/)
    cy.contains("Settings").click()

    cy.url().should('match', /.*\/jobs\/\d+\/settings$/)
    cy.contains("Delete job").click()

    cy.url().should('include', '/jobs')
    cy.contains('cypress_git_job').should('not.exist')
  })
})
